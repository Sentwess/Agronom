package com.example.agronom.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.agronom.R
import com.google.firebase.auth.FirebaseAuth


class SplashFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var splashLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

        val isLogin: Boolean = mAuth.currentUser != null
        val handler = Handler(Looper.myLooper()!!)
        handler.postDelayed({

            if (isLogin)
                navController.navigate(SplashFragmentDirections.actionSplashFragmentToSowingFragment())
            else
                navController.navigate(SplashFragmentDirections.actionSplashFragmentToSignInFragment())

        }, 2000)
    }

    private fun init(view: View) {
        mAuth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
        splashLayout = view.findViewById(R.id.splashLayout)
        imageView = view.findViewById(R.id.imageView)
        textView = view.findViewById(R.id.textView)
        val animationX = ObjectAnimator.ofFloat(splashLayout, "scaleX", 1F)
        val animationY = ObjectAnimator.ofFloat(splashLayout, "scaleY", 1F)
        val set = AnimatorSet()
        set.play(animationX)
            .with(animationY)
        set.duration = 800
        set.interpolator = DecelerateInterpolator()
        set.start()
    }
}