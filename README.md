# 详细介绍
[博客](https://www.jianshu.com/p/d10e147a2709)
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
