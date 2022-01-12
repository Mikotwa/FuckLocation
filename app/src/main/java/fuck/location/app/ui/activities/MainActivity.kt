package fuck.location.app.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import fuck.location.R
import fuck.location.databinding.ActivityMainBinding

import fuck.location.app.helpers.FakeLocationHelper

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val enabled = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var flhelper: FakeLocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.moduleStatusCard.setCardBackgroundColor(getColor(R.color.purple_500))
        binding.moduleStatusIcon.setImageDrawable(AppCompatResources.getDrawable(this,
            R.drawable.baseline_check_circle_24
        ))
        binding.moduleStatusText.text = "模块已经激活"
        binding.serviceStatusText.text = "请前往“设置”以对模块进行进一步设定"

        binding.serveTimes.text = "已合计为您服务 0 次"

        binding.menuDetectionTest.setOnClickListener(this)
        binding.menuLocationCredit.setOnClickListener(this)
        binding.menuSettings.setOnClickListener(this)

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
    }

    @SuppressLint("CheckResult")
    @ExperimentalStdlibApi
    override fun onClick(v: View) {
        when (v.id) {
            R.id.menu_detection_test -> {
                MaterialDialog(this).show {
                    title(text = "暂未写好")
                    message(text = "前面的区域，以后再来探索吧")
                }
            }

            R.id.menu_location_credit -> {
                val intent = Intent(this, ModuleActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_settings -> {
                val type = InputType.TYPE_CLASS_NUMBER
                var x: Float = 0F
                var y: Float = 0F

                setFakeLocation()
            }
        }
    }

    @ExperimentalStdlibApi
    private fun setFakeLocation() {
        val dialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(R.string.custom_location_dialog)
            customView(R.layout.custom_view_fakelocation, scrollable = true, horizontalPadding = true)
            positiveButton(R.string.custom_location_dialog_save) { dialog ->
                val yInput: EditText = dialog.getCustomView()   //TODO: 保存
                    .findViewById(R.id.custom_view_fl_y)
                val xInput: EditText = dialog.getCustomView()
                    .findViewById(R.id.custom_view_fl_x)

                flhelper = FakeLocationHelper.get()
                flhelper.writeFakeLocation(xInput.text.toString().toDouble(), yInput.text.toString().toDouble())
            }
            negativeButton(R.string.custom_location_dialog_notsave)
        }
    }
}