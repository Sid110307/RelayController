package com.sid.relaycontroller

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.commonEmptyRequestBody
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)
		setCurrentFragment(NormalModeFragment())

		if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
				Manifest.permission.ACCESS_NETWORK_STATE
			) != PackageManager.PERMISSION_GRANTED
		) registerForActivityResult(ActivityResultContracts.RequestPermission()) {
			if (!it) MaterialAlertDialogBuilder(this).setTitle("Permission denied")
				.setMessage("This app needs internet permission for communicating with the server.")
				.setPositiveButton("Grant") { _, _ ->
					requestPermissions(
						arrayOf(
							Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE
						), 0
					)
				}.setNegativeButton("Deny") { _, _ -> finish() }.show()
		}

		findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnItemSelectedListener {
			when (it.itemId) {
				R.id.normal_mode_fragment -> if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is NormalModeFragment) setCurrentFragment(
					NormalModeFragment()
				)

				R.id.expert_mode_fragment -> if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is ExpertModeFragment) setCurrentFragment(
					ExpertModeFragment()
				)

				else -> setCurrentFragment(NormalModeFragment())
			}

			true
		}

		if (getSharedPreferences("RelayController", Context.MODE_PRIVATE).getString(
				"IPAddress", null
			) != null
		) {
			ip = getSharedPreferences("RelayController", Context.MODE_PRIVATE).getString(
				"IPAddress", null
			)!!
			url = "https://${ip}:5000/relaycontroller"
		}

		hasError.observe(this) {
			if (it) {
				MaterialAlertDialogBuilder(this).setTitle("Error")
					.setMessage("Something went wrong. Make sure the server is running and the IP address is correct.")
					.setPositiveButton("OK") { _, _ -> hasError.postValue(false) }.show()

				NormalModeFragment.relay1.isChecked = false
				NormalModeFragment.relay2.isChecked = false
				NormalModeFragment.relay3.isChecked = false
				NormalModeFragment.relay4.isChecked = false
			}
		}

		findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout).setOnRefreshListener(::recreate)
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		super.onCreateOptionsMenu(menu)
		menuInflater.inflate(R.menu.menu_main, menu)

		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		val ipLayout = LinearLayout(this)
		ipLayout.orientation = LinearLayout.VERTICAL

		val addressText = EditText(this)
		addressText.hint = "IP address (e.g. 192.168.69.420)"
		addressText.inputType = InputType.TYPE_CLASS_TEXT
		addressText.setText(ip)

		ipLayout.addView(addressText)
		when (item.itemId) {
			R.id.info -> MaterialAlertDialogBuilder(this).setTitle("Info")
				.setMessage("This app is a relay controller for my Raspberry Pi.")
				.setPositiveButton("OK") { _, _ -> }.show()

			// TODO: Make the IP address persistent (probably using SharedPreferences)
			//  and fix the bug where the app crashes when the IP address is changed
			R.id.set_ip -> MaterialAlertDialogBuilder(this).setView(ipLayout)
				.setTitle("Enter the IP address of the server").setPositiveButton("OK") { _, _ ->
					val address = ipLayout.getChildAt(0) as EditText

					if (address.text.toString().trim()
							.matches(Regex("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}\$"))
					) {
						ip = address.text.toString().trim()
						url = "https://${ip}:5000/relaycontroller"

						val sharedPrefs =
							getSharedPreferences("RelayController", Context.MODE_PRIVATE)
						with(sharedPrefs.edit()) {
							putString("IPAddress", ip)
							commit()
						}

						Toast.makeText(this, "IP address set to $ip", Toast.LENGTH_SHORT).show()
						recreate()
					} else {
						address.error = "Invalid IP address"
						address.requestFocus()
					}
				}.setNegativeButton("Cancel") { _, _ -> }.show()
		}

		return super.onOptionsItemSelected(item)
	}

	private fun setCurrentFragment(fragment: Fragment) {
		supportFragmentManager.beginTransaction()
			.setCustomAnimations(R.anim.slide_right, R.anim.slide_left)
			.replace(R.id.fragment_container, fragment).commit()
	}

	companion object {
		private var ip = "127.0.0.1"

		var url = "https://${ip}:5000/relaycontroller"
		var hasError = MutableLiveData(false)
	}

	class HttpRequest(private val requestUrl: String) {
		var error = false
			private set

		private fun getClient(): OkHttpClient {
			val trustAllCerts =
				arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager") object :
					X509TrustManager {
					override fun checkClientTrusted(
						chain: Array<X509Certificate?>?, authType: String?
					) {
						Log.d("NormalModeFragment", "checkClientTrusted")
					}

					override fun checkServerTrusted(
						chain: Array<X509Certificate?>?, authType: String?
					) {
						Log.d("NormalModeFragment", "checkServerTrusted")
					}

					override fun getAcceptedIssuers(): Array<X509Certificate?> {
						Log.d("NormalModeFragment", "getAcceptedIssuers")
						return arrayOf()
					}
				})

			val sslContext: SSLContext = SSLContext.getInstance("SSL")
			sslContext.init(null, trustAllCerts, SecureRandom())
			val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

			val builder = OkHttpClient.Builder()
			builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
			builder.hostnameVerifier { _, _ -> true }

			return builder.build()
		}

		fun execute() {
			getClient().newCall(
				Request.Builder().method("POST", commonEmptyRequestBody).url(requestUrl).build()
			).enqueue(object : okhttp3.Callback {
				override fun onFailure(call: Call, e: java.io.IOException) {
					Log.e("HttpRequest", "Failed to execute request: ${e.cause}")

					hasError.postValue(true)
					error = true
				}

				override fun onResponse(call: Call, response: Response) {
					val responseBody = response.body.string()

					if (responseBody.trim() == "") Log.d("HttpRequest", "Success") else {
						Log.e("HttpRequest", "$requestUrl failed: $responseBody")

						hasError.postValue(true)
						error = true
					}
				}
			})
		}
	}
}
