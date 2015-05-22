<?php
$site['title'] = 'Login';

$message = '';
if(isset($_POST['submit'])){
	$errors = array();
	if($_POST['email'] == ''){
		$errors[] = 'Please enter your e-mail address.';
	}
	if($_POST['password'] == ''){
		$errors[] = 'Please enter your password.';
	}
	$user = array();
	if(count($errors) == 0){
		establishDatabaseConnection();
		$user = pg_fetch_assoc(@pg_query('select "id", "passwordHash", "deletable" from "user" where "email" = '.pg_escape_literal($_POST['email'])));
		pg_close();
		if($user['id'] == '' or $user['passwordHash'] != hash('sha256', $_POST['password'])){
			$errors[] = 'There is no user having these credentials.';
		}
		if(!$user['deletable'] and !in_array($user['id'], $config['users'])){
			$errors[] = 'You have to be an administrator to access this page.';
		}
	}
	if(count($errors) == 0){
		$_SESSION['id'] = $user['id'];
		header('Location: '.$_SERVER['REQUEST_URI']);
		die();
	}
	else{
		$message = createMessage(implode('<br />', $errors));
	}
}
?>
<h1><?=$site['title']?></h1>
<?=$message?>
<form action="" method="post">
 <div class="row">
   <label for="email">E-mail address:</label>
   <input type="text" id="email" name="email" value="<?=$_POST['email']?>" />
 </div>
 <div class="row">
   <label for="password">Password:</label>
   <input type="password" id="password" name="password" value="" />
 </div>
 <div class="row">
  <input type="submit" name="submit" value="Log in" />
 </div>
</form>