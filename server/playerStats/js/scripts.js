$(document).ready(function(){
	initDataTables();
});

function initDataTables(){
	if($('table.dataTable:not(.settingTable)').length == 0){
		return;
	}
	$('table.dataTable:not(.settingTable)').selectable({
		'autoRefresh': false,
		'cancel': 'a, .header',
		'filter': 'tr:not(.header)',
		'stop': function(){
			var selected = [];
			$.each($('table.dataTable tr.ui-selected'), function(){
				selected.push($('td:first-child', this).text());
			});
			$('#whereId').val(selected);
		}
	});
	
	var i = 1;
	$.each($('td', $('table.dataTable:not(.settingTable) tr.header')[0]), function(){
		$('div.dataTableHelper table.dataTable tr.header td:nth-of-type(' + i + ')').width($(this).width());
		i++;
	});
	
	$(window)
	.scroll(function(){
		var headerPosition = $($('table.dataTable:not(.settingTable) tr.header')[0]).offset().top;
		var windowPosition = window.scrollY;
		if(windowPosition > headerPosition){
			$('div.dataTableHelper').show(0);
		}
		else{
			$('div.dataTableHelper').hide(0);
		}
	});
	
	$('table.dataTable:not(.settingTable) tr.header .order').change(function(){
		var form = $('form')[0];
		$.each($('select.order'), function(){
			if($(this).val() != ''){
				var tmp = '<input type="hidden" />';
				$(form).append($(tmp).attr('name', $(this).attr('name')).val($(this).val()));
			}
		});
		form.submit();
	});
	
	$('.dataTablePagination .restriction .limit').change(function(){
		$('#limit').val($(this).val());
		$('form')[0].submit();
	});
	
	$('input.date').datepicker({'dateFormat': 'yy-mm-dd'})
	.change(function(){
		$('#' + $(this).attr('name')).val($(this).val());
		$('form')[0].submit();
	});
}