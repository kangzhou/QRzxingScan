# 最终效果
为放大效果，特意找来一部低性能的测试机
### 优化之前
统计多次扫描结果，单帧识别成功的时间为2-5S

![1](https://github.com/kangzhou/QRzxingScan/blob/master/pic/zxingscan11.png)

### 优化之后
![2](https://github.com/kangzhou/QRzxingScan/blob/master/pic/zxingscan22.png)

基本维持在50ms左右

# How to
### Step 1
```java 
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
### Step 2
```java 
	dependencies {
	        implementation 'com.github.kangzhou:QRzxingScan:1.0.2'
	}
```
### Step 3
```java 
public void goScan(View view){
        Intent intent = new Intent(this, QRScanActivity.class);
        startActivityForResult(intent,SCAN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&&requestCode==SCAN_CODE){
            Toast.makeText(this,data.getStringExtra("code"),Toast.LENGTH_SHORT).show();
        }
    }
```

# 原理
	1. 减少解码格式提高解码速度，二维码格式是QR Code，一维码格式为Code 128
	2. 解码算法优化。使用GlobalHistogramBinarizer算法的效果要稍微比HybridBinarizer好一些，识别的速度更快
	3. 减少解码数据，（1）裁剪无用区域，减少解码数据。（2）灰度处理
	4. 串行请求处理帧改为并发处理帧（可选）
	5. 降低bestReView的size，从而降低分辨率
其中减少解码数据和降低分辨率是个人觉得最有效的方式

# 自定义
如果你不满意我的扫码界面，想自定义的话，我为各位看官准备了ZxingCallBack接口
```java 
public interface ZxingCallBack {
    /**
     * 扫码结果
     * @param rawResult 结果
     * @param barcode 识别图片
     */
    void scanResult(Result rawResult, Bitmap barcode);

    /**
     * 扫码框
     * @return
     */
    View getScopImage();

    /**
     * @return SurfaceView
     */
    SurfaceView getSurfaceView();

    /**
     * 当前activity
     * @return
     */
    Activity getContext();
}
```
具体可参看QRScanActivity的实现
