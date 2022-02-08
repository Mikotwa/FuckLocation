package fuck.location.app.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import fuck.location.R
import fuck.location.databinding.ActivityMainBinding

import fuck.location.xposed.helpers.ConfigGateway

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setModuleState(binding)

        binding.menuDetectionTest.setOnClickListener(this)
        binding.menuLocationCredit.setOnClickListener(this)
        binding.menuSettings.setOnClickListener(this)
        binding.menuAbout.setOnClickListener(this)

        setContentView(binding.root)
    }

    @SuppressLint("CheckResult")
    @ExperimentalStdlibApi
    override fun onClick(v: View) {
        when (v.id) {
            R.id.menu_detection_test -> {
                MaterialDialog(this).show {
                    title(text = getString(R.string.dialog_not_available_dialog))
                    message(text = getString(R.string.dialog_not_available_content))
                }
            }

            R.id.menu_location_credit -> {
                val intent = Intent(this, ModuleActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_settings -> {
                setFakeLocation()
            }

            R.id.menu_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
        }
    }

    @ExperimentalStdlibApi
    private fun setFakeLocation() {
        MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(R.string.custom_location_dialog)
            customView(R.layout.custom_view_fakelocation, scrollable = true, horizontalPadding = true)

            ConfigGateway.get().setCustomContext(applicationContext)
            val previousYInput = ConfigGateway.get().readFakeLocation()?.y
            val previousXInput = ConfigGateway.get().readFakeLocation()?.x
            val previousECIInput = ConfigGateway.get().readFakeLocation()?.eci
            val previousPCIInput = ConfigGateway.get().readFakeLocation()?.pci
            val previousTACInput = ConfigGateway.get().readFakeLocation()?.tac
            val previousEarfCNInput = ConfigGateway.get().readFakeLocation()?.earfcn
            val previousBandwidthInput = ConfigGateway.get().readFakeLocation()?.bandwidth

            findViewById<EditText>(R.id.custom_view_fl_x).setText(previousXInput.toString())
            findViewById<EditText>(R.id.custom_view_fl_y).setText(previousYInput.toString())
            findViewById<EditText>(R.id.custom_view_fl_eci).setText(previousECIInput.toString())
            findViewById<EditText>(R.id.custom_view_fl_pci).setText(previousPCIInput.toString())
            findViewById<EditText>(R.id.custom_view_fl_tac).setText(previousTACInput.toString())
            findViewById<EditText>(R.id.custom_view_fl_earfcn).setText(previousEarfCNInput.toString())
            findViewById<EditText>(R.id.custom_view_fl_bandwidth).setText(previousBandwidthInput.toString())

            positiveButton(R.string.custom_location_dialog_save) { dialog ->
                val yInput: EditText = dialog.getCustomView()
                    .findViewById(R.id.custom_view_fl_y)
                val xInput: EditText = dialog.getCustomView()
                    .findViewById(R.id.custom_view_fl_x)
                val eciInput: EditText = dialog.getCustomView()
                    .findViewById(R.id.custom_view_fl_eci)
                val pciInput: EditText = dialog.getCustomView()
                    .findViewById(R.id.custom_view_fl_pci)
                val tacInput: EditText = dialog.getCustomView()
                    .findViewById(R.id.custom_view_fl_tac)
                val earfcnInput: EditText = dialog.getCustomView()
                    .findViewById(R.id.custom_view_fl_earfcn)
                val bandwidthInput: EditText = dialog.getCustomView()
                    .findViewById(R.id.custom_view_fl_bandwidth)

                ConfigGateway.get().writeFakeLocation(
                    xInput.text.toString().toDouble(),
                    yInput.text.toString().toDouble(),
                    eciInput.text.toString().toInt(),
                    pciInput.text.toString().toInt(),
                    tacInput.text.toString().toInt(),
                    earfcnInput.text.toString().toInt(),
                    bandwidthInput.text.toString().toInt()
                )
            }
            negativeButton(R.string.custom_location_dialog_notsave)
        }
    }

    private fun setModuleState(binding: ActivityMainBinding) {
        if (isModuleActivated()) {
            binding.moduleStatusCard.setCardBackgroundColor(getColor(R.color.purple_500))
            binding.moduleStatusIcon.setImageDrawable(AppCompatResources.getDrawable(this,
                R.drawable.baseline_check_circle_24
            ))
            binding.moduleStatusText.text = getString(R.string.card_title_activated)
            binding.serviceStatusText.text = getString(R.string.card_detail_activated)

            binding.serveTimes.text = getString(R.string.card_serve_time)
        } else {
            binding.moduleStatusCard.setCardBackgroundColor(getColor(R.color.red_500))
            binding.moduleStatusIcon.setImageDrawable(AppCompatResources.getDrawable(this,
                R.drawable.baseline_error_24
            ))
            binding.moduleStatusText.text = getText(R.string.card_title_not_activated)
            binding.serviceStatusText.text = getText(R.string.card_detail_not_activated)
            binding.serveTimes.visibility = View.GONE

            binding.menuDetectionTest.visibility = View.GONE
            binding.menuLocationCredit.visibility = View.GONE
            binding.menuSettings.visibility = View.GONE
        }
    }

    @Keep
    fun isModuleActivated(): Boolean {
        return false
    }
}