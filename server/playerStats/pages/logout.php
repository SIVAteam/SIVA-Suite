<?php
$site['title'] = 'Logout...';
unset($_SESSION);
session_destroy();
?>
<script type="text/javascript">
 $(document).ready(function(){
	window.location.href = '/';
 });
</script>