/**
 * EXTENSIBLE FORMS
 * jQuery Plugin
 * Copyright 2014 by Christian Handschigl
 * www.handschigl.com
 */
(function($){
	$.fn.extensibleForm = function(options){
		return this.each(function(){
			var form = new ExtensibleForm(this, options);
			form.init();
		});
	};
}(jQuery));

function ExtensibleForm(form, options){

	this.form = form;

	this.rowCounter = 0;

	this.options = options;
	
	this.init = function(){
		this.rowCounter = $('.extensibleFormRow', this.form).length;
  		$('.extensibleFormTemplate', this.form).hide(0);
  		if(this.options && this.options.onInit){
			this.options.onInit();
		}
  		this.bindEvents(this.rowCounter);
  		this.rebuild();
  	};
  	
    this.bindEvents = function(){
    	var thisObject = this;
    	$('input.extensibleFormCondition', this.form).unbind().change(function(){
    		thisObject.rebuild();
		});
    	$('.extensibleFormRemove', this.form).unbind().click(function(){
    		thisObject.removeRow($(this).closest('.extensibleFormRow'));
		});
		if(this.options && this.options.onBindEvents){
			this.options.onBindEvents();
		}
    };
    
    this.rebuild = function(){
    	var thisObject = this;
    	var lastVisibleField = $('.extensibleFormNewRow .extensibleFormCondition', this.form);
    	if(lastVisibleField.length != 0){
    		lastVisibleField = lastVisibleField[0];
	    }
	    var addNewRow = true;
	    $.each($('.extensibleFormRow:visible', this.form), function(){
	    	var field = $('.extensibleFormCondition', this);
			if(field.length > 0){
				if(($(field).attr('type') == 'checkbox' && !$(field).prop('checked')) || ($(field).attr('type') != 'checkbox' && $(field).val() == '')){
					if(field[0] != lastVisibleField){
						$(this).hide(500, function(){
							this.remove();
						});
					}
					else{
						addNewRow = false;
					}
				}
			}
		});
		if(addNewRow){
    		$('.extensibleFormRemove', this.form).show(0);
    		$('.extensibleFormNewRow', this.form).removeClass('extensibleFormNewRow');
			var newRow = $('.extensibleFormTemplate', this.form).clone().removeClass('extensibleFormTemplate').addClass('extensibleFormNewRow').insertBefore($('.extensibleFormTemplate', this.form)).show(500);
    		$('.extensibleFormRemove', newRow).hide(0);
			$.each($('input, select, textarea', newRow), function(){
				$(this).attr('name', $(this).attr('name').replace(/0/, thisObject.rowCounter));
			});
    		this.rowCounter++;
		}
		if(this.options && this.options.onRebuild){
			this.options.onRebuild();
		}
		this.bindEvents();
    };

    this.removeRow = function(row){
    	var field = $('.extensibleFormCondition', row);
    	if($(field).attr('type') == 'checkbox'){
	    	$(field).prop('checked', false);
    	}
    	else{
	    	$(field).val('');
		}
    	if(this.options && this.options.onRemoveRow){
			this.options.onRemoveRow();
		}
		this.rebuild();
	};
}