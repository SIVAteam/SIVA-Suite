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
		var queryString = getParameterMap();
		queryString['offset'] = 0;
		if($(this).val() == ''){
			delete queryString[$(this).attr('name')];
		}
		else{
			queryString[$(this).attr('name')] = $(this).val();
		}
		window.location.href = '?' + generateDataTableQueryString(queryString);
	});
	
	$('.dataTablePagination .restriction .limit').change(function(){
		var queryString = getParameterMap();
		queryString['offset'] = 0;
		queryString['limit'] = $(this).val();
		window.location.href = '?' + generateDataTableQueryString(queryString);
	});
	
	$('input.date').datepicker({'dateFormat': 'yy-mm-dd'})
	.change(function(){
		var queryString = getParameterMap();
		queryString['offset'] = 0;
		if($(this).val() == ''){
			delete queryString[$(this).attr('name')];
		}
		else{
			queryString[$(this).attr('name')] = $(this).val();
		}
		window.location.href = '?' + generateDataTableQueryString(queryString);
	});
}

function getParameterMap(){
    var map = {};
	var params = window.location.search.replace(/^\?/, '').split('&');
	for(var i = 0; i < params.length; i++){
		if(params[i].length > 0){
			var param = params[i].split('=');
			map[decodeURIComponent(param[0])] = decodeURIComponent(param[1]);
		}
	}
	return map;
}

function generateDataTableQueryString(params){
	var queryString = [];
	for(var key in params){
		queryString.push(params[key] = encodeURIComponent(key) + '=' + encodeURIComponent(params[key]));
	}
	return queryString.join('&');
}