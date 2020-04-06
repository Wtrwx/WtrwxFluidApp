# WtrwxFluidApp

Hexo-theme-fluid-AndroidApp  

##### 使用说明：  

1.请务必确保您的hexo主题为Fluid，不是请自行适配jsoup脚本。  

2.在全局范围内修改https://wtrwx.top为您的域名，注意不可多/少/，需保持与原格式一致。  

3.在您的博客中全局添加以下js代码（判断UA隐藏元素）。 

```JavaScript
if (navigator.userAgent == "app/WtrwxFluid") {
    document.getElementById("header").style.height = "0";
    document.getElementById("navbar").style.display = "none";
    //console.log('app');
}
```

##### 开源相关：  

1.[AndroidX](https://developer.android.com/topic/libraries/support-library/androidx-rn)  

2.[jsoup](https://jsoup.org/)  

3.[BottomSheetMenu](https://github.com/krossovochkin/BottomSheetMenu)  
