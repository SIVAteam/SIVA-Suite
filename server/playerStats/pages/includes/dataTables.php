<div class="dataTablePagination">
 <span class="restriction">
  Results: <span class="results"><?=$amount['amount']?></span>
  From: <input type="text" name="from" value="<?=$_GET['from']?>" class="date" />
  To: <input type="text" name="to" value="<?=$_GET['to']?>" class="date" />
  Rows per page:
  <select name="limit" class="limit">
   <option>25</option>
   <option <?=(($limit == 50) ? 'selected="selected"' : '')?>>50</option>
   <option <?=(($limit == 100) ? 'selected="selected"' : '')?>>100</option>
   <option <?=(($limit == 250) ? 'selected="selected"' : '')?>>250</option>
   <option <?=(($limit == 500) ? 'selected="selected"' : '')?>>500</option>
  </select>
 </span>
 <span>Page:</span>
 <?php
 $pages = '';
 $currentPage = round($offset / $limit, 0);
 $maxPage = round($amount['amount'] / $limit, 0);
 $meanPage = round($maxPage / 2, 0);
 $placeholder = false;
 for($i = 0; $i * $limit < $amount['amount']; $i++){
	if(!($i <= 15 or ($i > $meanPage - 7 and $i <= $meanPage + 7) or $i > $maxPage - 15 or ($i > $currentPage - 7 and $i <= $currentPage + 7))){
		if(!$placeholder){
			$placeholder = true;
			$pages .= '<span class="placeholder">...</span>';
		}
		continue;
	}
	$placeholder = false;
	$tmp = $_GET;
	$tmp['offset'] = $i * $limit;
	$pages .= '<a href="?'.generateDataTableQueryString($tmp).'" '.(($i == $currentPage) ? 'class="current"' : '').'>'.($i + 1).'</a>';
 }
 echo $pages;
 ?>
</div>
<table class="dataTable">
 <?php 
 $result = @pg_query($query) or throwMessage(pg_last_error());
 $i = 0;
 $header = '';
 while($row = pg_fetch_assoc($result)){
	if($i == 0){
		$cols = array_keys($row);
		$header = '<tr class="header">';
		foreach($cols as $col){
			$header .= '<td>'.$col.'<select class="order" name="sort['.$col.']"><option value="">Sort</option><option value="ASC" '.(($_GET['sort'][$col] == 'ASC') ? 'selected="selected"' : '').'>ASC</option><option value="DESC" '.(($_GET['sort'][$col] == 'DESC') ? 'selected="selected"' : '').'>DESC</option></select></td>';
		}
		$header .= '</tr>';
		echo $header;
	}
	?>
	<tr>
	 <?php
	 foreach($row as $col => $value){
		if(isset($linked_cols[$col])){
			$value = '<a href="'.str_replace(urlencode('{value}'), urlencode($value), str_replace('{value}', urlencode($value), $linked_cols[$col])).'">'.$value.'</a>';
		}
		if($col == 'eventTime'){
			$value = date('Y-m-d H:i:s', $value / 1000);
		}
		?>
		<td><?=$value?></td>
		<?php
	 }
	 ?>
	</tr>
	<?php
	$i++;
 }
 ?>
</table>
<?if($amount['amount'] > 0):?>
<div class="dataTablePagination bottom">
 <span class="restriction">
  Results: <span class="results"><?=$amount['amount']?></span>
  From: <input type="text" name="from" value="<?=$_GET['from']?>" class="date" />
  To: <input type="text" name="to" value="<?=$_GET['to']?>" class="date" />
  Rows per page:
  <select name="limit" class="limit">
   <option>25</option>
   <option <?=(($limit == 50) ? 'selected="selected"' : '')?>>50</option>
   <option <?=(($limit == 100) ? 'selected="selected"' : '')?>>100</option>
   <option <?=(($limit == 250) ? 'selected="selected"' : '')?>>250</option>
   <option <?=(($limit == 500) ? 'selected="selected"' : '')?>>500</option>
  </select>
 </span>
 <span>Page:</span> <?=$pages?>
</div>
<?endif;?>
<div class="dataTableHelper">
 <table class="dataTable">
  <?=$header?>
 </table>
</div>