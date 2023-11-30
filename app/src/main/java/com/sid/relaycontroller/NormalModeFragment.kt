package com.sid.relaycontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.sid.relaycontroller.MainActivity.Companion.url

class NormalModeFragment : Fragment() {

	private var relay1Checked = false
	private var relay2Checked = false
	private var relay3Checked = false
	private var relay4Checked = false

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		val v = inflater.inflate(R.layout.fragment_normal_mode, container, false)

		relay1 = v.findViewById(R.id.relay1)
		relay2 = v.findViewById(R.id.relay2)
		relay3 = v.findViewById(R.id.relay3)
		relay4 = v.findViewById(R.id.relay4)

		relay1.setOnCheckedChangeListener { btn, isChecked ->
			if (!btn.isPressed) return@setOnCheckedChangeListener
			btn.isEnabled = false

			relay1Checked = isChecked
			MainActivity.HttpRequest("$url/relay1/").execute()

			btn.isEnabled = true
		}

		relay2.setOnCheckedChangeListener { btn, isChecked ->
			if (!btn.isPressed) return@setOnCheckedChangeListener
			btn.isEnabled = false

			relay2Checked = isChecked
			MainActivity.HttpRequest("$url/relay2/").execute()

			btn.isEnabled = true
		}

		relay3.setOnCheckedChangeListener { btn, isChecked ->
			if (!btn.isPressed) return@setOnCheckedChangeListener
			btn.isEnabled = false

			relay3Checked = isChecked
			MainActivity.HttpRequest("$url/relay3/").execute()

			btn.isEnabled = true
		}

		relay4.setOnCheckedChangeListener { btn, isChecked ->
			if (!btn.isPressed) return@setOnCheckedChangeListener
			btn.isEnabled = false

			relay4Checked = isChecked
			MainActivity.HttpRequest("$url/relay4/").execute()

			btn.isEnabled = true
		}

		v.findViewById<MaterialButton>(R.id.toggle_all).setOnClickListener {
			relay1.isChecked = !relay1.isChecked
			relay2.isChecked = !relay2.isChecked
			relay3.isChecked = !relay3.isChecked
			relay4.isChecked = !relay4.isChecked

			relay1Checked = relay1.isChecked
			relay2Checked = relay2.isChecked
			relay3Checked = relay3.isChecked
			relay4Checked = relay4.isChecked

			MainActivity.HttpRequest("$url/relay1/").execute()
			MainActivity.HttpRequest("$url/relay2/").execute()
			MainActivity.HttpRequest("$url/relay3/").execute()
			MainActivity.HttpRequest("$url/relay4/").execute()
		}

		v.findViewById<MaterialButton>(R.id.toggle_all).setOnLongClickListener {
			relay1.isChecked = false
			relay2.isChecked = false
			relay3.isChecked = false
			relay4.isChecked = false

			Snackbar.make(v, "All switches turned off", Snackbar.LENGTH_SHORT).show()
			true
		}

		return v
	}

	override fun onResume() {
		super.onResume()

		relay1.isChecked = relay1Checked
		relay2.isChecked = relay2Checked
		relay3.isChecked = relay3Checked
		relay4.isChecked = relay4Checked
	}

	companion object {
		lateinit var relay1: SwitchMaterial
		lateinit var relay2: SwitchMaterial
		lateinit var relay3: SwitchMaterial
		lateinit var relay4: SwitchMaterial
	}
}