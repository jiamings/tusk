<?php
header ( 'Content-Type: text/html; charset=UTF-8' );

// 调试模式开关
define ( 'DEBUG_MODE', false );

if (! function_exists ( 'curl_init' )) {
	echo '您的服务器不支持 PHP 的 Curl 模块，请安装或与服务器管理员联系。';
	exit ();
}

// App Key
define ( "APP_KEY", 'c8cbeedd74d5422e912da13216f26103' );
// App Secret
define ( "APP_SECRET", 'cf7fbec444f64385b168d38302fa4d89' );
// 应用回调页地址
define ( "CALLBACK_URL", "http://grayluck.vicp.cc/examples/callback.php" );

if (DEBUG_MODE) {
	error_reporting ( E_ALL );
	ini_set ( 'display_errors', true );
}
