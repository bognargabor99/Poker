package hu.bme.aut.onlab.poker

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import hu.bme.aut.onlab.poker.databinding.FragmentAuthBinding
import hu.bme.aut.onlab.poker.model.UserAuthInfo
import hu.bme.aut.onlab.poker.network.PokerAPI
import hu.bme.aut.onlab.poker.utils.toBase64
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.util.concurrent.Executors

@DelicateCoroutinesApi
@OptIn(InternalAPI::class)
class AuthFragment : DialogFragment() {
    private lateinit var binding: FragmentAuthBinding

    private val PREFS_NAME = "PokerPreferences"
    private val PREF_USERNAME = "username"
    private val PREF_PASSWORD = "password"
    private val client = HttpClient(CIO)

    private val dispatcher: CoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val isValid: Boolean
        get() {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            return username.isNotBlank() && password.isNotBlank() && password.length >= 8
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthBinding.inflate(layoutInflater, container, false)

        loadRememberedAuthInfo()
        setTextChangedListeners()
        setOnClickListeners()

        return binding.root
    }

    private fun loadRememberedAuthInfo() {
        val pref = activity?.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val usernamePref = pref?.getString(PREF_USERNAME, "")
        val passwordPref = pref?.getString(PREF_PASSWORD, "")

        binding.etUsername.setText(usernamePref)
        binding.etPassword.setText(passwordPref)
        binding.btnLogin.isEnabled = isValid
        binding.btnRegister.isEnabled = isValid
        if (usernamePref?.isNotBlank() == true && passwordPref?.isNotBlank() == true)
            binding.cbRemember.isChecked = true
    }

    private fun setTextChangedListeners() {
        binding.etUsername.addTextChangedListener {
            binding.btnLogin.isEnabled = isValid
            binding.btnRegister.isEnabled = isValid
        }

        binding.etPassword.addTextChangedListener {
            binding.btnLogin.isEnabled = isValid
            binding.btnRegister.isEnabled = isValid
        }
    }

    private fun setOnClickListeners() {

        binding.btnGuest.setOnClickListener {
            PokerAPI.connect(MainFragment.POKER_DOMAIN, UserAuthInfo("",""), {
                MainFragment._this.interactionEnabled = true
            }) {
                MainFragment._this.interactionEnabled = false
            }
            dismiss()
        }
        binding.btnLogin.setOnClickListener {
            val result = GlobalScope.async { tryLogin().toString() }
            GlobalScope.launch {
                val stringResult = result.await()
                if (stringResult.toBoolean()) {
                    val username = binding.etUsername.text.toString()
                    val password = binding.etPassword.text.toString()
                    val remember = binding.cbRemember.isChecked
                    if (remember)
                        savePreferences(username, password)
                    else
                        savePreferences()

                    PokerAPI.connect(MainFragment.POKER_DOMAIN, UserAuthInfo(username, password), {
                        MainFragment._this.interactionEnabled = true
                    }) {
                        MainFragment._this.interactionEnabled = false
                    }

                    dismiss()
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Couldn't log in with the given credentials", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            val result = GlobalScope.async { tryRegister() }
            GlobalScope.launch {
                val booleanResult = result.await()
                if (booleanResult)
                    activity?.runOnUiThread {
                        binding.btnLogin.performClick()
                    }
            }
        }
    }

    private fun savePreferences(name: String = "", psw: String = "") {
        activity?.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            ?.edit()
            ?.putString(PREF_USERNAME, name)
            ?.putString(PREF_PASSWORD, psw)
            ?.apply()
    }

    private suspend fun tryRegister(): Boolean {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        return withContext(dispatcher) {
            try {
                val response: HttpResponse = client.post("https://${MainFragment.POKER_DOMAIN}.ngrok.io/register") {
                    headers {
                        append(HttpHeaders.Accept, "text/html")
                        append(HttpHeaders.Accept, "application/json")
                        append(HttpHeaders.ContentType, "application/json")
                        append(HttpHeaders.UserAgent, "ktor poker client")
                        val requestBody = Gson().toJson(UserAuthInfo(username, password))
                        body = requestBody
                    }
                }
                return@withContext response.status.isSuccess()
            } catch (e: ClientRequestException) {
                return@withContext false
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }
    }

    private suspend fun tryLogin(): Boolean {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        return withContext(dispatcher) {
            try {
                val response: HttpResponse = client.get("https://${MainFragment.POKER_DOMAIN}.ngrok.io/authenticate") {
                    headers {
                        append(HttpHeaders.Accept, "text/html")
                        append(HttpHeaders.Accept, "application/json")
                        append(HttpHeaders.Authorization, "Basic ${"$username:$password".toBase64()}")
                        append(HttpHeaders.UserAgent, "ktor poker client")
                    }
                }
                return@withContext response.status.isSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }
    }
}