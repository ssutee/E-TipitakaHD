package com.watnapp.etipitaka.plus

import androidx.fragment.app.FragmentActivity

/**
 * Empty FragmentActivity host for instrumented tests that attach real
 * fragments (e.g. detached-fragment regression tests).
 *
 * Lives in the debug source set (not androidTest) because a host activity
 * must be part of the APP APK: an activity declared in the test APK cannot
 * run in the instrumented app process — its classloader only contains the
 * test APK, so androidx classes fail with ClassNotFoundException. Same
 * approach as androidx fragment-testing's EmptyFragmentActivity.
 */
class TestFragmentHostActivity : FragmentActivity()
