<?php
	session_start ();

	include_once ('config.php');
	include_once ('../rennclient/RennClient.php');

	echo getcwd() . " ";

	$renn_client = new RennClient ( APP_KEY, APP_SECRET );
	$renn_client->setDebug ( DEBUG_MODE );
	// 获得保存的token
	$renn_client->authWithStoredToken ();
	// 获得用户接口
	$user_service = $renn_client->getUserService ();
	$blogServ = $renn_client->getBlogService ();
	// 获得指定用户
	// $user = $user_service->getUser ( 431695399 );
	$usrLst = array(600638900, 600435535, 600376271, 601303385, 600806598, 600992694, 600977841);
	$usrNam = array("学生清华", "清华社团", "清华大学学生会", "新清华学堂", "清华大学清新时报", "清华电视台", "清华大学学生科协");
	$fi = fopen('output.txt', 'a');
	foreach($usrLst as $ind => $value)
	{
		for($i = 10; $i < 100; ++$i)
		{
			$fname = ''.$value.'_'.$i.'.txt';
			print_r($fname);
			$blogs = $blogServ->listBlog($value, 20, $i);
			foreach($blogs as $blog)
			{
				$s = str_replace(",", "_", $blog['content']);
				$s = str_replace("\n", " ", $s);
				$s = str_replace("\r", " ", $s);
				fprintf($fi, "%s,%s,%s\n", $blog['title'], $s, $usrNam[$ind]);
			}
			sleep(1);
			break;
		}
	}
	fclose($fi);
	echo "finish.";
?>
