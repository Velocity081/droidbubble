package com.droidbubble.app.service

import android.graphics.Rect
import android.graphics.Region
import android.view.View
import android.view.ViewTreeObserver
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * Manages touch pass-through for the full-width overlay window.
 * Uses ViewTreeObserver.OnComputeInternalInsetsListener to ensure that only
 * the dynamic island pill (and auxiliary bubble) intercepts touch events when compact,
 * allowing touches on the status bar (clock, battery, Wi-Fi) and underlying apps
 * to pass directly through without any window resize or repositioning glitches.
 */
class IslandTouchManager(private val overlayView: View) {

    private var insetsListener: Any? = null
    private var isRegistered = false

    @Volatile
    var isExpanded = false

    @Volatile
    var isOccupied = false

    private val compactRects = mutableListOf<Rect>()

    private val attachStateListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            registerListener()
        }
        override fun onViewDetachedFromWindow(v: View) {
            isRegistered = false
        }
    }

    fun updateCompactBounds(rects: List<Rect>, occupied: Boolean, expanded: Boolean) {
        synchronized(compactRects) {
            compactRects.clear()
            compactRects.addAll(rects)
            isOccupied = occupied
            isExpanded = expanded
        }
        overlayView.post {
            overlayView.requestLayout()
            overlayView.invalidate()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
                overlayView.requestApplyInsets()
            }
        }
    }

    fun attach() {
        overlayView.addOnAttachStateChangeListener(attachStateListener)
        registerListener()
    }

    fun detach() {
        overlayView.removeOnAttachStateChangeListener(attachStateListener)
        if (!isRegistered || insetsListener == null) return
        try {
            val listenerClass = Class.forName("android.view.ViewTreeObserver\$OnComputeInternalInsetsListener")
            val removeMethod = ViewTreeObserver::class.java.getMethod("removeOnComputeInternalInsetsListener", listenerClass)
            removeMethod.invoke(overlayView.viewTreeObserver, insetsListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isRegistered = false
        insetsListener = null
    }

    private fun registerListener() {
        if (isRegistered && overlayView.viewTreeObserver.isAlive) return
        try {
            val vto = overlayView.viewTreeObserver
            if (!vto.isAlive) return

            val listenerClass = Class.forName("android.view.ViewTreeObserver\$OnComputeInternalInsetsListener")
            val addMethod = ViewTreeObserver::class.java.getMethod("addOnComputeInternalInsetsListener", listenerClass)

            if (insetsListener == null) {
                val handler = InvocationHandler { _, method, args ->
                    if (method.name == "onComputeInternalInsets" && args != null && args.isNotEmpty()) {
                        val insetsInfo = args[0]
                        applyInsets(insetsInfo)
                    }
                    null
                }
                insetsListener = Proxy.newProxyInstance(listenerClass.classLoader, arrayOf(listenerClass), handler)
            }

            addMethod.invoke(vto, insetsListener)
            isRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyInsets(insetsInfo: Any) {
        try {
            val setTouchableInsetsMethod = insetsInfo.javaClass.getMethod("setTouchableInsets", Int::class.javaPrimitiveType)
            val touchableRegionField = insetsInfo.javaClass.getField("touchableRegion")
            val region = touchableRegionField.get(insetsInfo) as Region

            synchronized(compactRects) {
                if (isExpanded) {
                    // TOUCHABLE_INSETS_FRAME = 0: entire window captures touches so outside taps collapse island
                    setTouchableInsetsMethod.invoke(insetsInfo, 0)
                } else if (isOccupied && compactRects.isNotEmpty()) {
                    // TOUCHABLE_INSETS_REGION = 3: only exact island bounds capture touches
                    setTouchableInsetsMethod.invoke(insetsInfo, 3)
                    region.setEmpty()
                    for (rect in compactRects) {
                        region.op(rect, Region.Op.UNION)
                    }
                } else {
                    // Completely click-through when idle
                    setTouchableInsetsMethod.invoke(insetsInfo, 3)
                    region.setEmpty()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
