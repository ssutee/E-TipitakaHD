package com.watnapp.etipitaka.plus.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.watnapp.etipitaka.plus.account.AccountScreen
import com.watnapp.etipitaka.plus.account.AccountViewModel
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountActivity : AppCompatActivity() {

    private val viewModel: AccountViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ETipitakaTheme {
                AccountScreen(viewModel)
            }
        }
    }
}
