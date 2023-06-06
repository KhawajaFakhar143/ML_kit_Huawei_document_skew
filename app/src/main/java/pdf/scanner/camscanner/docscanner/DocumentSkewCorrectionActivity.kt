package pdf.scanner.camscanner.docscanner

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerFactory
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionAnalyzerSetting
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionConstant
import com.huawei.hms.mlsdk.dsc.MLDocumentSkewCorrectionCoordinateInput
import java.io.IOException

class DocumentSkewCorrectionActivity : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    private lateinit var imageView: ImageView
    private lateinit var documentScanView: DocumentCorrectImageView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_skew_correction)
        documentScanView = findViewById(R.id.iv_documetscan)
        val inputStream = assets.open("IMG_20230526_123327.jpg")
        bitmap = BitmapFactory.decodeStream(inputStream)
         imageView= findViewById(R.id.document_skew_image_view)
        imageView.setImageBitmap(bitmap)
        performDocumentSkewCorrection(bitmap)
    }
    private fun performDocumentSkewCorrection(bitmap: Bitmap){
        var setting = MLDocumentSkewCorrectionAnalyzerSetting.Factory().create()
        val analyzer = MLDocumentSkewCorrectionAnalyzerFactory.getInstance()
            .getDocumentSkewCorrectionAnalyzer(setting)

        var frame = MLFrame.fromBitmap(bitmap)
        val detectTask = analyzer!!.asyncDocumentSkewDetect(frame)
        detectTask.addOnSuccessListener {
            // Detection success.
            val leftTop = it.leftTopPosition
            val rightTop = it.rightTopPosition
            val leftBottom = it.leftBottomPosition
            val rightBottom = it.rightBottomPosition

            val coordinates: MutableList<Point> = ArrayList()
            coordinates.add(leftTop)
            coordinates.add(rightTop)
            coordinates.add(rightBottom)
            coordinates.add(leftBottom)
            imageView.visibility = View.GONE
            documentScanView.setImageBitmap(bitmap)
            documentScanView.setPoints(coordinates.toTypedArray())
          //  val coordinateData = MLDocumentSkewCorrectionCoordinateInput(coordinates)
           // val correctionTask = analyzer.asyncDocumentSkewCorrect(frame, coordinateData)
//            correctionTask.addOnSuccessListener {result ->
//                // Detection success.
//               // imageView.setImageBitmap(result.corrected)
//            }.addOnFailureListener {
//                // Detection failure.
//            }
            // Call the syncDocumentSkewCorrect synchronous method.
          //  val correct = analyzer.syncDocumentSkewCorrect(frame, coordinateData)
        //    if (correct != null && correct[0].resultCode == MLDocumentSkewCorrectionConstant.SUCCESS) {
                // Correction success.
         //   } else {
// Correction failure.
        //    }
        }.addOnFailureListener {
            // Detection failure.
        }
        val detect = analyzer.analyseFrame(frame)
// Call the synchronous method analyseFrame.
        if (detect != null && detect[0].resultCode == MLDocumentSkewCorrectionConstant.SUCCESS) {
            // Detection success.
        } else {
// Detection failure.
        }

        val detectResult = detect!![0]

        try {
            analyzer.stop()
        } catch (e: IOException) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}