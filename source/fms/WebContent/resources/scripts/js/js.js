$(document).ready(function(){

showTimeMin();

/* Moveing gadgets */
$(function() {$(".vertsortable").sortable({opacity: 0.6, handle: '.vertsortable_head'});});
$(function() {$(".horizsortable").sortable({opacity: 0.6, handle: '.horizsortable_head'});});

/* Datapicker */
$(function() {$('#datepicker').datepicker({inline:true});});

/* show or hide gadgets */
  $('a[rel="hide_block"]').click(function(){
	if ($(this).parent('div').parent('div').children('div.gadgetblock').css('height')=='auto') $(this).css('background-image','url(images/button_dropup_bl.gif)');
	else $(this).css('background-image','url(images/button_dropdown_bl.gif)');
    $(this).parent('div').parent('div').children('div.gadgetblock').slideToggle();
	return false;});

/* show or hide error-message */
  $('a[rel="hide_err"]').click(function(){
	var color='';
	if ($(this).parent('div').parent('div').hasClass('err_or')) color='_or';
	else if ($(this).parent('div').parent('div').hasClass('err_bl')) color='_bl';
	else if ($(this).parent('div').parent('div').hasClass('err_gr')) color='_gr';
	if ($(this).parent('div').parent('div').children('div.error_msg').css('height')=='auto') $(this).css('background-image','url(images/button_dropup'+color+'.gif)');
	else $(this).css('background-image','url(images/button_dropdown'+color+'.gif)');
    $(this).parent('div').parent('div').children('div.error_msg').slideToggle();
	return false;});

/* close error-message */
  $('a[rel="close_err"]').click(function(){
	$(this).parent('div').parent('div').css('display','none');
	return false;});
										 

/* dialogs */

	//select all the a tag with name equal to modal
	$('a[name=modal]').click(function(e) {
		//Cancel the link behavior
		e.preventDefault();
		
		//Get the A tag
		var id = $(this).attr('href');
	
		//Get the screen height and width
		var maskHeight = $(document).height();
		var maskWidth = $(window).width();
	
		//Set heigth and width to mask to fill up the whole screen
		$('#mask').css({'width':maskWidth,'height':maskHeight});
		
		//transition effect		
		$('#mask').fadeIn(1000);	
		$('#mask').fadeTo("slow",0.8);	
	
		//Get the window height and width
		var winH = $(window).height();
		var winW = $(window).width();
              
		//Set the popup window to center
		$(id).css('top',  winH/2-$(id).height()/2);
		$(id).css('left', winW/2-$(id).width()/2);
	
		//transition effect
		$(id).fadeIn(2000); 
	
	});
	
	//if close button is clicked
	$('.window .close').click(function (e) {
		//Cancel the link behavior
		e.preventDefault();
		
		$('#mask').hide();
		$('.window').hide();
	});		
	
	//if mask is clicked
	$('#mask').click(function () {
		$(this).hide();
		$('.window').hide();
	});			
	

});
