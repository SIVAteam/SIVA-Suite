var SIVA_PLAYER_DEFAULT_PRIMARY_COLOR = '#1173AA';
var SIVA_PLAYER_DEFAULT_SECONDARY_COLOR = '#ffffff';

function SivaPlayer(DOMObject, arrayPosition){
	this.player = DOMObject;
	
	this.arrayPosition = arrayPosition;
		
	this.configuration = {};
	
	this.active = true;
	
	this.playedBefore = [];
	
	this.currentScene;
	
	this.controlsDisplayTime = 0;
	
	this.currentSceneTime = 0;
	
	this.currentSceneEnded = false;
	
	this.lastTimeObject = {};
	
	this.preventControlsFadeOut = true;
	
	this.volume = 1;
	
	this.previousVolume = -1;
	
	this.volumeSetByUser = true;
	
	this.history = [];
	
	this.sceneHistory = {};
	
	this.isAnnotationSidebarVisible = false;
	
	this.annotationSidebarOpener = '';
	
	this.isAnnotationVisible = true;
	
	this.isSubTitleVisible = true;
	
	this.visibleSidebarAnnotationType = 'sivaPlayer_authorAnnotations';
	
	this.hashchangeCounter = 0;
	
	this.currentLanguage;
	
	this.log = [];
	
	this.logKey;
	
	this.logCounter = 0;
	
	this.initTime = 0;
	
	this.isMobile = false;
	
	this.playingMediaAnnotations = [];
	
	this.mode = 'default';
	
	this.init = function(config, noUserOverwrites){
		var thisPlayer = this;
		this.preparePlayer();
		this.startLoader();
		this.compabilityCheck();
		this.setConfiguration(config, noUserOverwrites);
		if(!sivaPlayerStorage.hasChromeLocalStorage && !sivaPlayerStorage.hasLocalStorage){
			this.throwError(new Error(this.getLabel('noStorage')), false);
		}
		if(this.configuration.accessRestriction && !this.configuration.accessRestriction.passed){
			this.handleAccessRestriction();
		}
		else if(this.configuration.common.secretKey && this.configuration.common.secretKey != '' && !this.configuration.common.keygenEmail && !this.configuration.common.keygenCode){
			this.handleKeygenRestriction();
		}
		else if(this.configuration.common.useSecretLogin && !this.configuration.common.userEmail && !this.configuration.common.userSecret){
			this.handleSecretLogin();
		}
		else{
			sivaPlayerStorage.getAll(function(result){
				if(result.disabledTitle && result.disabledTitle != '' && result.disabledText && result.disabledText != ''){
					thisPlayer.stopLoader();
					thisPlayer.createPopup('message', false);
					$('.sivaPlayer_messagePopup .sivaPlayer_title', thisPlayer.player).text(result.disabledTitle);
					$('.sivaPlayer_messagePopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).html(result.disabledText);
					return;
				}
				if(result.volume){
					var volume = JSON.parse(result.volume);
					if(volume.time > (new Date()).getTime() - 3 * 60 * 60 * 1000){
						thisPlayer.volume = volume.percent;
					}
				}
				var i = 0;
				for(var key in result){
					if(key.indexOf('sivaPlayerLog_') == 0){
						i++;
					}
				}
				if(i > 30){
					thisPlayer.stopLoader();
					thisPlayer.createPopup('message', false);
					$('.sivaPlayer_messagePopup .sivaPlayer_title', thisPlayer.player).text(thisPlayer.getLabel('syncRequiredTitle'));
					$('.sivaPlayer_messagePopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).text(thisPlayer.getLabel('syncRequiredText'));
				}
				else{
					thisPlayer.initTime = (new Date()).getTime();
					if(sivaPlayerStorage.hasChromeLocalStorage){
						thisPlayer.logAction('getClientInformation', 'chromeApp', '');
					}
					thisPlayer.checkIncludedLanguages();
					thisPlayer.createCustomStyles();
					var startScene = $.bbq.getState(thisPlayer.arrayPosition);
					if(!startScene)
						startScene = thisPlayer.configuration.startScene;
					thisPlayer.setNextNode(startScene, thisPlayer.configuration.common.autoStart);
					thisPlayer.logClientInformation();
					thisPlayer.syncLog();
				}
			});
		}
	};
	
	this.preparePlayer = function(){
		thisPlayer = this;
		FastClick.attach(this.player);
		var lastPosition = {'x': 0, 'y': 0};
		$(this.player).addClass('sivaPlayer_arrayPosition' +  this.arrayPosition)
		.mousemove(function(e){
			if(e.clientX != lastPosition.x || e.clientY != lastPosition.y){
				lastPosition.x = e.clientX;
				lastPosition.y = e.clientY;
				thisPlayer.fadeInFadeOutControls(3);
			}
		});
		$(document).unbind('keyup').keyup(function(e){
			if(e.keyCode == '27'){
				if(!document.fullScreen && !document.mozFullScreen && !document.webkitIsFullScreen){
					if($('.sivaPlayer_popup .sivaPlayer_closeButton, .sivaPlayer_searchSidebar', thisPlayer.player).length > 0){
						thisPlayer.logAction('useKey', 'ESC', '');
						thisPlayer.closePopups(600);
					}
					else if($('.sivaPlayer_annotation.sivaPlayer_fullscreen', thisPlayer.player).length > 0){
						thisPlayer.closeMediaAnnotationFullscreen($('.sivaPlayer_annotation.sivaPlayer_fullscreen', thisPlayer.player)[0]);
					}
				}
			}
			else if(e.keyCode == '32'){
				if($(e.target).prop('tagName') == 'INPUT'){
					return;
				}
				else if($('.sivaPlayer_popup .sivaPlayer_closeButton, .sivaPlayer_searchSidebar', thisPlayer.player).length > 0){
					thisPlayer.logAction('useKey', 'SPACE', '');
					thisPlayer.closePopups(600);
				}
				else if($('.sivaPlayer_annotation.sivaPlayer_fullscreen', thisPlayer.player).length > 0){
					var mediaElement = $('.sivaPlayer_annotation.sivaPlayer_fullscreen', thisPlayer.player).find('video, audio')[0];
					thisPlayer.logAction('useKey', 'SPACE', ((mediaElement.paused) ? 'play' : 'pause'));
					if(mediaElement.paused){
						mediaElement.play();
					}
					else{
						mediaElement.pause();
					}				
				}
				else{
					var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player);
					if(videoElement.length > 0){
						thisPlayer.logAction('useKey', 'SPACE', ((videoElement[0].paused) ? 'play' : 'pause'));
						if(videoElement[0].paused){
							thisPlayer.playVideo();
						}
						else{
							thisPlayer.pauseVideo();
						}
					}
				}
			}
		});
		$(document).keydown(function(e) {
			if($(e.target).prop('tagName') == 'INPUT'){
				return true;
			}
			else if(e.keyCode == 32) {
		        return false;
		    }
		});
	};
	
	this.startLoader = function(){
		var loader = $('.sivaPlayer_loader', this.player);
		if(loader.length == 0){
			$(this.player).append('<table class="sivaPlayer_loader"><tr><td class="sivaPlayer_holder"><span class="sivaPlayer_holder2"><span class="sivaPlayer_logo">SIVA Player <span>3.0</span></span><span class="sivaPlayer_text">Loading...</span></span></td></tr><tr><td class="sivaPlayer_loading"><span class="sivaPlayer_loadingDots"><span class="sivaPlayer_loadingDot"></span><span class="sivaPlayer_loadingDot"></span><span class="sivaPlayer_loadingDot"></span><span class="sivaPlayer_loadingDot"></span><span class="sivaPlayer_loadingDot"></span><span class="sivaPlayer_loadingDot"></span><span class="sivaPlayer_loadingDot"></span></span></td></tr></table>')
			.fadeIn(800);
			this.animateLoading();
		}
		else if($('.sivaPlayer_loader:visible', this.player).length == 0){
			if(this.configuration.videoPath){
				$('.sivaPlayer_loader .sivaPlayer_logo', this.player).hide(0);
				$('.sivaPlayer_loader .sivaPlayer_text', this.player).text(this.getLabel('loading'));
			}
			loader.show(0);
			this.animateLoading();
		}
	};
	
	this.stopLoader = function(){
		$('.sivaPlayer_loader', this.player).fadeOut(800);
	};
	
	this.animateLoading = function(left){
		var thisPlayer = this;
		if($('.sivaPlayer_loader:visible', this.player).length > 0){
			var dots = $('.sivaPlayer_loading .sivaPlayer_loadingDots', this.player);
			if(left == undefined || left == 105){
				dots.removeClass('sivaPlayer_transition')
				.css({'left': '-15%'});
				left = -15;
			}
			else if(left == -15){
				dots.addClass('sivaPlayer_transition');
				left = 35;
			}
			else if(left == 35){
				left = 55;
			}
			else if(left == 55){
				left = 105;
			}
			dots.css({'left': left + '%'});
			setTimeout(function(){
				thisPlayer.animateLoading(left);
			}, ((left == -15) ? 50 : 800));
		}
	};
	
	this.compabilityCheck = function(){
		if(!Modernizr.video){
			this.throwError(new Error(this.getLabel('noHTML5VideoSupportError')), false);
		}
		if(!Modernizr.video.webm && !Modernizr.video.h264){
			this.throwError(new Error(this.getLabel('missingVideoCodecError')), false);
		}
		if(!Modernizr.audio.mp3 && !Modernizr.audio.ogg && !Modernizr.audio.mp4){
			this.throwError(new Error(this.getLabel('missingAudioCodecError')), false);
		}
		if(!Modernizr.history && !Modernizr.hashchange){
			this.throwError(new Error(this.getLabel('noHTML5HistorySupportError')), false);
		}
		if(!Modernizr.inlinesvg){
			this.throwError(new Error(this.getLabel('noSVGSupportError')), false);
		}
		if(/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Nokia|Symbian|Kindle|Silk|Opera Mini/i.test(window.navigator.userAgent)){
			this.isMobile = true;
		}
	};
	
	this.setConfiguration = function(config, noUserOverwrites){				
		if(!config && (typeof sivaVideoConfiguration == 'undefined' || sivaVideoConfiguration.length == 0)){
			this.throwError(new Error('Video configuration erroneous or not found.'), false);
		}
		
		if(!config && sivaVideoConfiguration.length == 0){
			this.throwError(new Error('An error ocurred while loading the configuration file.'), false);
		}
		
		this.configuration = config;
		if(!config){
			this.configuration = sivaVideoConfiguration.shift();
		}
		
		this.configuration.configPath = this.configuration.configPath.split('?')[0];
		
		var path = this.configuration.configPath.split('/');
		path.pop();
		path.pop();
		path = path.join('/');
		this.configuration.videoPath = path;
		
		if(this.configuration.videoPath == ''){
			this.throwError(new Error('No path for video files found.'), false);
		}
		
		this.configuration.supportedVideoTypes = [];
		this.configuration.ratio = 16 / 9;
		
		if(!noUserOverwrites){
			$.each($('.sivaPlayer_configuration span', this.player), function(){
				var option = $(this).attr('class').split('_');
				var value = $(this).text();
				if(value == 'true')
					value = true;
				else if(value == 'false')
					value = false;
				if(option.length == 1){
					thisPlayer.configuration[(option[0])] = value;
				}
				else if(option.length == 2){
					if(!thisPlayer.configuration[(option[0])]){
						thisPlayer.configuration[(option[0])] = {};
					}
					thisPlayer.configuration[(option[0])][(option[1])] = value;
				}
				else if(option.length == 3){
					if(!thisPlayer.configuration[(option[0])]){
						thisPlayer.configuration[(option[0])] = {};
					}
					if(!thisPlayer.configuration[(option[0])][(option[1])]){
						thisPlayer.configuration[(option[0])][(option[1])] = {};
					}
					thisPlayer.configuration[(option[0])][(option[1])][(option[2])] = value;
				}
			});
		}
		
		this.isAnnotationSidebarVisible = (this.configuration.common.annotationSidebarVisibility == 'always' || this.configuration.common.annotationSidebarVisibility == 'onStart');
		
		this.currentLanguage = this.configuration.defaultLanguage;
	};
	
	this.createCustomStyles = function(){
		if(!this.configuration.style.primaryColor && ! this.configuration.style.secondaryColor)
			return;
		var primaryColor = ((this.configuration.style.primaryColor) ? this.configuration.style.primaryColor : SIVA_PLAYER_DEFAULT_PRIMARY_COLOR);
		var secondaryColor = ((this.configuration.style.secondaryColor) ? this.configuration.style.secondaryColor : SIVA_PLAYER_DEFAULT_SECONDARY_COLOR);
		this.configuration.primaryColor = primaryColor;
		this.configuration.secondaryColor = secondaryColor;
		var playerClass = '.sivaPlayer_arrayPosition' + this.arrayPosition;
		var css = [{'property': 'background', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_videoContainer .sivaPlayer_annotation audio', '.sivaPlayer_loading .sivaPlayer_loadingDot', '.sivaPlayer_popup', '.sivaPlayer_controls', '.sivaPlayer_volumeControl', '.sivaPlayer_annotationSidebar', '.sivaPlayer_nodeSelectionSidebar', '.sivaPlayer_searchSidebar', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations.sivaPlayer_active', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations.sivaPlayer_active', '.sivaPlayer_markerButton span', '.sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timelineMarker', '.sivaPlayer_mediaAnnotationControls .sivaPlayer_timeline .sivaPlayer_timelineProgress span span', '.sivaPlayer_annotationSidebarButton span']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_popup label input', '.sivaPlayer_popup label button', '.sivaPlayer_searchSidebar input', '.sivaPlayer_sceneList a', '.sivaPlayer_timeline .sivaPlayer_timelineProgress span span', '.sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timlineUpdateSelectedTime', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations', '.sivaPlayer_videoContainer .sivaPlayer_annotation .sivaPlayer_mediaAnnotationControls', '.sivaPlayer_annotationSidebar .sivaPlayer_mediaAnnotationControls', '.sivaPlayer_zoomPopup .sivaPlayer_content .sivaPlayer_mediaAnnotationControls', '.sivaPlayer_annotation.sivaPlayer_fullscreen .sivaPlayer_closeButton', '.sivaPlayer_annotation.sivaPlayer_fullscreen .sivaPlayer_title', '.sivaPlayer_videoContainer .sivaPlayer_annotation .sivaPlayer_title', '.sivaPlayer_videoContainer .sivaPlayer_richtextAnnotation', '.sivaPlayer_volumeControl .sivaPlayer_volume.sivaPlayer_current', '.sivaPlayer_popup.sivaPlayer_zoomPopup', '.sivaPlayer_popup.sivaPlayer_pdfPopup', '.sivaPlayer_popup.sivaPlayer_richtextPopup', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar']},{'value': 'rgba(' + this.hexToRgb(secondaryColor) + ', 0.5)', 'selectors': ['.sivaPlayer_videoContainer .sivaPlayer_galleryAnnotation']},{'value': 'rgba(' + this.hexToRgb(secondaryColor) + ', 0.7)', 'selectors': ['.sivaPlayer_volumeControl .sivaPlayer_volume.sivaPlayer_hover']}]},
					 {'property': 'color', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_accessRestrictionPopup label input', '.sivaPlayer_accessRestrictionPopup label button', '.sivaPlayer_searchSidebar input', '.sivaPlayer_sceneList a', '.sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timlineUpdateSelectedTime', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations', '.sivaPlayer_videoContainer .sivaPlayer_annotation .sivaPlayer_mediaAnnotationControls', '.sivaPlayer_annotationSidebar .sivaPlayer_mediaAnnotationControls', '.sivaPlayer_zoomPopup .sivaPlayer_content .sivaPlayer_mediaAnnotationControls', '.sivaPlayer_popup label input', '.sivaPlayer_popup label button', '.sivaPlayer_annotation.sivaPlayer_fullscreen .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_zoomPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_pdfPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_richtextPopup .sivaPlayer_title', '.sivaPlayer_videoContainer .sivaPlayer_annotation', '.sivaPlayer_videoContainer .sivaPlayer_annotation a', '.sivaPlayer_volumeControl .sivaPlayer_volume.sivaPlayer_current', '.sivaPlayer_volumeControl .sivaPlayer_volume.sivaPlayer_hover', '.sivaPlayer_popup.sivaPlayer_zoomPopup', '.sivaPlayer_popup.sivaPlayer_pdfPopup', '.sivaPlayer_popup.sivaPlayer_richtextPopup', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar']}, {'value': secondaryColor, 'selectors': ['', '.sivaPlayer_searchSidebar .sivaPlayer_results a', '.sivaPlayer_popup', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations.sivaPlayer_active', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations.sivaPlayer_active', '.sivaPlayer_tableOfContents a', '.sivaPlayer_tableOfContents span', '.sivaPlayer_markerButton span', '.sivaPlayer_annotation a', '.sivaPlayer_popup .sivaPlayer_title']}]},
					 {'property': 'fill', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_mediaAnnotationControls .sivaPlayer_button svg', '.sivaPlayer_annotation.sivaPlayer_fullscreen .sivaPlayer_closeButton svg', '.sivaPlayer_popup.sivaPlayer_zoomPopup .sivaPlayer_closeButton svg', '.sivaPlayer_popup.sivaPlayer_pdfPopup .sivaPlayer_closeButton svg', '.sivaPlayer_popup.sivaPlayer_richtextPopup .sivaPlayer_closeButton svg', '.sivaPlayer_overlayButton svg polygon', '.sivaPlayer_overlayButton svg path', '.sivaPlayer_statsPopup svg .day', '.sivaPlayer_button.sivaPlayer_galleryPreviousButton', '.sivaPlayer_button.sivaPlayer_galleryNextButton']}, {'value': 'rgba(' + this.hexToRgb(secondaryColor) + ', 0.2)', 'selectors': ['.sivaPlayer_markerEllipse', '.sivaPlayer_markerRectangle', '.sivaPlayer_markerPolygon']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_button', '.sivaPlayer_button svg', '.sivaPlayer_tableOfContents svg', '.sivaPlayer_overlayButton svg circle', '.sivaPlayer_button.sivaPlayer_galleryPreviousButton circle', '.sivaPlayer_button.sivaPlayer_galleryNextButton circle', '.sivaPlayer_statsPopup svg text', '.sivaPlayer_popup .sivaPlayer_closeButton svg']}]},
					 {'property': 'border-color', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timelineUpdate span', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations', '.sivaPlayer_mediaAnnotationControls .sivaPlayer_timeline .sivaPlayer_timelineProgress', '.sivaPlayer_annotationSidebarButton', '.sivaPlayer_annotation.sivaPlayer_fullscreen .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_zoomPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_pdfPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_richtextPopup .sivaPlayer_title']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_sceneList a', '.sivaPlayer_timeline .sivaPlayer_timelineProgress', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations.sivaPlayer_active', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations.sivaPlayer_active', '.sivaPlayer_sidebarTabs .sivaPlayer_spacer', '.sivaPlayer_annotationSidebar > div.sivaPlayer_active .sivaPlayer_annotation', '.sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timelineMarker', '.sivaPlayer_mediaAnnotationControls .sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timelineUpdate span', '.sivaPlayer_volumeControl .sivaPlayer_volume', '.sivaPlayer_annotation.sivaPlayer_fullscreen .sivaPlayer_title', '.sivaPlayer_popup .sivaPlayer_title']}]},
					 {'property': 'border-bottom-color', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations.sivaPlayer_active', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations.sivaPlayer_active']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations']}, {'value': '1px solid rgba(' + this.hexToRgb(secondaryColor) + ', 0.3)', 'selectors': ['.sivaPlayer_searchSidebar .sivaPlayer_results a']}]},
					 {'property': 'stroke', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_markerEllipse', '.sivaPlayer_markerRectangle', '.sivaPlayer_markerPolygon']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_statsButton svg line', '.sivaPlayer_statsButton svg line', '.sivaPlayer_statsPopup svg .day', '.sivaPlayer_statsPopup svg .month']}]}
					];		
		var styles = '';
		for(var i = 0; i < css.length; i++){
			for(var j = 0; j < css[i].values.length; j++){
				var selectors = [];
				for(var k = 0; k < css[i].values[j].selectors.length; k++){
					selectors[k] = playerClass + '.sivaPlayer' + ((css[i].values[j].selectors[k].indexOf('portrait') > -1 || css[i].values[j].selectors[k].indexOf('landscape') > -1) ? '' : ' ') + css[i].values[j].selectors[k];
				}
				styles += selectors.join(',') + '{' + css[i].property + ':' + css[i].values[j].value + '}';
			}
		}
		var safari = navigator.userAgent.split('Safari/');
		if(safari.length > 1 && safari[1].split('.')[0] < 536){
			styles += playerClass + ' td.sivaPlayer_button{width:35px;} ' + playerClass + '.sivaPlayer_closeButton svg{max-width:52px;}';
		}
		$(this.player).prepend('<style>' + styles + '</style>');
	};
	
	this.onResize = function(){
		this.fadeInFadeOutControls(3);
		if($('.sivaPlayer_statsPopup', this.player).length > 0){
			this.createStats();
		}
		this.setProportions();
		if(!this.configuration.accessRestriction || this.configuration.accessRestriction.passed){
			this.updateAnnotations();
		}
	};
	
	this.setProportions = function(){
		console.log('prop');
		var playerOffset = $(this.player).offset();
		if(this.configuration.style && this.configuration.style.width){
			$(this.player).css('width', this.configuration.style.width);
		}
		var playerWidth = $(this.player).width();
		if(this.configuration.style && this.configuration.style.height){
			var height = this.configuration.style.height;
			if(height == 'auto'){
				height = parseInt(playerWidth / this.configuration.ratio);
			}
			$(this.player).css('height', height);		
		}
		var playerHeight = $(this.player).height();
		if(playerWidth <= 400){
			$(this.player).removeClass('sivaPlayer_landscape').addClass('sivaPlayer_portrait');
			this.mode = 'portrait';
		}
		else if(playerWidth <= 700){
			$(this.player).removeClass('sivaPlayer_portrait').addClass('sivaPlayer_landscape');
			this.mode = 'landscape';
		}
		else{
			$(this.player).removeClass('sivaPlayer_portrait').removeClass('sivaPlayer_landscape');
			this.mode = 'default';
		}
		var video = $('.sivaPlayer_mainVideo', this.player);
		if(video.length > 0 && !$('.sivaPlayer_annotationSidebar', this.player).hasClass('sivaPlayer_update')){
			$('.sivaPlayer_videoContainer', this.player).css(this.getVideoContainerProportions(video, playerWidth, playerHeight, this.isAnnotationSidebarVisible, false));
		}
		var sidebar = $('.sivaPlayer_annotationSidebar', this.player);
		console.log(this.mode);
		if(this.mode == 'portrait'){
			
		}
		else if(this.mode == 'landscape'){
			
		}
		else{
			
		}
		setTimeout(function(){
			$('.sivaPlayer_annotationSidebar .sivaPlayer_authorAnnotations, .sivaPlayer_annotationSidebar .sivaPlayer_communityAnnotations', thisPlayer.player).css('height', ($(sidebar).height() - $('.sivaPlayer_sidebarTabs', sidebar).height() - 5 ) + 'px');
		}, 100);
		var volumeButton = $('.sivaPlayer_controls .sivaPlayer_volumeButton', this.player);
		if(volumeButton.length > 0){
			var safari = navigator.userAgent.split('Safari/');
			if(safari.length > 1 && safari[1].split('.')[0] < 536){
				playerWidth -= 5;
			}
			$('.sivaPlayer_controls .sivaPlayer_volumeControl', this.player).css('right', (playerOffset.left + playerWidth - 50 / 2 - 40 / 2 - $(volumeButton[0]).offset().left) + 'px');
		}
		$.each($('.sivaPlayer_overlayButton', this.player), function(){
			$(this).css({'margin-top': parseInt($(this).height() / -2) + 'px', 'margin-left': parseInt($(this).width() / -2) + 'px'});
		});
		$.each($('.sivaPlayer_popup .sivaPlayer_scrollable', this.player), function(){
			var popup = $(this).closest('.sivaPlayer_popup');
			var height = playerHeight;
			$.each($('tr', popup), function(){
				if($('.sivaPlayer_content', this).length == 0){
					height -= $(this).height();
				}
				else{
					height -= parseInt($('.sivaPlayer_content', this).css('padding-top'));
					height -= parseInt($('.sivaPlayer_content', this).css('padding-bottom'));
				}
			});
			$(this).height(height);
		});
		if($('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).length > 0){
			var tmpWidth = playerWidth - parseInt($('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).css('padding-left')) - parseInt($('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).css('padding-right')); 
			var tmpHeight = playerHeight;
			if($('.sivaPlayer_zoomPopup tr.sivaPlayer_galleryThumbnails', this.player).length> 0){
				tmpHeight *= 0.85;
			}
			tmpHeight = tmpHeight - $('.sivaPlayer_zoomPopup .sivaPlayer_title', this.player).height() - parseInt($('.sivaPlayer_zoomPopup .sivaPlayer_title', this.player).css('padding-top')) - parseInt($('.sivaPlayer_zoomPopup .sivaPlayer_title', this.player).css('padding-bottom'));
			$('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).css({'height': tmpHeight, 'width': tmpWidth});
			$('.sivaPlayer_zoomPopup tr.sivaPlayer_galleryThumbnails div', this.player).width(playerWidth + 'px');
			$('.sivaPlayer_zoomPopup tr.sivaPlayer_galleryThumbnails td img', this.player).height(parseInt(playerHeight * 0.14) + 'px');
			$('.sivaPlayer_zoomPopup .sivaPlayer_content img', this.player).css({
				'max-height': tmpHeight + 'px',
				'max-width': tmpWidth + 'px'
			});
			var buttons = $('.sivaPlayer_zoomPopup .sivaPlayer_galleryPreviousButton, .sivaPlayer_zoomPopup .sivaPlayer_galleryNextButton', this.player);
			$(buttons).css('margin-top', parseInt($(buttons).height() / -2) + 'px');
		}
		if($('.sivaPlayer_pdfPopup .sivaPlayer_content', this.player).length > 0){
			console.log(playerHeight, $('.sivaPlayer_pdfPopup .sivaPlayer_title', this.player).height());
			$('.sivaPlayer_pdfPopup .sivaPlayer_content', this.player).height(playerHeight - $('.sivaPlayer_pdfPopup .sivaPlayer_title', this.player).height() - parseInt($('.sivaPlayer_pdfPopup .sivaPlayer_title', this.player).css('padding-top')) - parseInt($('.sivaPlayer_pdfPopup .sivaPlayer_title', this.player).css('padding-bottom')));
		}
		if($('.sivaPlayer_annotationSidebar .sivaPlayer_timeline, .sivaPlayer_videoContainer .sivaPlayer_timeline', this.player).width() < 80){
			$('.sivaPlayer_contronls .sivaPlayer_timeline', this.player).addClass('sivaPlayer_reducedTimeline');
		}
		else{
			$('.sivaPlayer_contronls .sivaPlayer_timeline', this.player).removeClass('sivaPlayer_reducedTimeline');
		}
		$.each($('.sivaPlayer_mediaAnnotationControls'), function(){
			var tmp = $(this).parent().width();
			if($(this).parents('.sivaPlayer_annotationSidebar').length > 0){
				tmp = $('.sivaPlayer_annotationSidebar').width() - ($('.sivaPlayer_button', this).length * 26);
			}
			if(tmp < 105){
				$('.sivaPlayer_timeline, .sivaPlayer_timelineDuration', this).hide(0);
			}
			else if(tmp < 155){
				$('.sivaPlayer_timeline', this).hide(0);
				$('.sivaPlayer_timelineDuration', this).show(0);
			}
			else{
				$('.sivaPlayer_timeline, .sivaPlayer_timelineDuration', this).show(0);
			}
		});
		$.each($('.sivaPlayer_annotation.sivaPlayer_fullscreen', this.player), function(){
			$('video', this).css('height', $(thisPlayer.player).height() - $('.sivaPlayer_title', this).height() - parseInt($('.sivaPlayer_title', this).css('padding-top')) - parseInt($('.sivaPlayer_title', this).css('padding-bottom')) - $('.sivaPlayer_mediaAnnotationControlsHolder', this).height());
		});
		if(this.configuration.style){
			$('.sivaPlayer_searchSidebar', this.player).css('width', parseInt(this.configuration.style.annotationSidebarWidth * $(this.player).width() + 40) + 'px');
		}
	};
	
	this.getVideoContainerProportions = function(videoElements, playerWidth, playerHeight, isAnnotationSidebarVisible, isAnnotationSidebarVisible2){
		var videoWidth = videoElements[0].videoWidth;
		var videoHeight = videoElements[0].videoHeight;
		var containerWidth = playerWidth;
		var containerHeight = playerHeight;
		if(this.mode == 'default' && isAnnotationSidebarVisible && ($('.sivaPlayer_annotationSidebarButton.sivaPlayer_open', this.player).length > 0 || isAnnotationSidebarVisible2) && !this.configuration.common.annotationSidebarOverlay){
			containerWidth -= thisPlayer.configuration.style.annotationSidebarWidth * playerWidth;
		}
		else if(this.mode == 'landscape'){
			containerWidth *= 0.6;
		}
		else if(this.mode == 'portrait'){
			containerHeight *= 0.6;
		}
		var factor = videoWidth / containerWidth;
		videoWidth = containerWidth;
		videoHeight /= factor;
		if(videoHeight > containerHeight){
			factor = videoHeight / containerHeight;
			videoHeight = containerHeight;
			videoWidth /= factor;
		}
		return {
			'width': Math.round(videoWidth) + 'px',
			'height': Math.round(videoHeight) + 'px',
			'margin-top': Math.round((containerHeight - videoHeight) / 2) + 'px',
			'margin-right': Math.round((containerWidth - videoWidth) / 2) + 'px',
			'margin-bottom': Math.round((containerHeight - videoHeight) / 2) + 'px',
			'margin-left': Math.round((containerWidth - videoWidth) / 2) + 'px'
		};
	};
	
	this.handleSecretLogin = function(message, message2){
		var thisPlayer = this;
		sivaPlayerStorage.get('secretLogin', function(login){
			if(!login){
				var title = thisPlayer.getLabel('loginRequiredTitle');
				var content = '<label><span>' + thisPlayer.getLabel('usernameField') + ':</span> <input type="text" name="username" /></label><label><span>' + thisPlayer.getLabel('passwordField') + ':</span> <input type="password" name="password" /></label>';
				thisPlayer.createPopup('accessRestriction', false);
				$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_title', thisPlayer.player).text(title);
				$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).append('<form>' + content + '<label><span></span> <button>' + thisPlayer.getLabel('getAccessButton') + '</button></label><label class="sivaPlayer_noAccount">' + thisPlayer.getLabel('forgottenText') + ' <a href="' + thisPlayer.configuration.common.logPath.split('/sivaPlayerVideos')[0] + '/xhtml/users/recoverPassword.jsf" target="_blank" title="' + thisPlayer.getLabel('forgottenTooltip') + '">' + thisPlayer.getLabel('forgottenLink') + '</a></label></form>');
				if(message){
					$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).prepend('<span class="sivaPlayer_message">' + thisPlayer.getLabel(message) + ((message2) ? '(' + message2 + ')' : '') + '</span>');
				}
				$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content button', thisPlayer.player).click(function(e){
					thisPlayer.startLoader();
					var params = $('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content form', thisPlayer.player).serialize();
					$('.sivaPlayer_accessRestrictionPopup', thisPlayer.player).remove();
					$.ajax({
						'async': true,
						'data': 'ajax=true&' + params,
						'dataType': 'JSON',
						'crossDomain': true,
						'timeout': 60000,
						'type': 'POST',
						'url': thisPlayer.configuration.common.logPath + '/getSecret.js',
						'error': function(e){
							thisPlayer.handleSecretLogin('commonErrorTitle', e.responseText);
						},
						'success': function(data){
							if(data.userEmail && data.userSecret){
								sivaPlayerStorage.set('secretLogin', JSON.stringify(data));
							}
							thisPlayer.handleSecretLogin(data.description);
						}
					});
					e.preventDefault();
				});
				thisPlayer.stopLoader();
			}
			else{
				login = JSON.parse(login);
				thisPlayer.configuration.common.userEmail = login.userEmail;
				thisPlayer.configuration.common.userSecret = login.userSecret;
				thisPlayer.init(thisPlayer.configuration, true);
			}
		});
	};
	
	this.handleAccessRestriction = function(intendedAction){
		var thisPlayer = this;
		if(this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken){
			$.ajax({
				'async': true,
				'data': {'ajax': 'true', 'token': this.configuration.accessRestriction.accessToken},
				'dataType': 'JSON',
				'crossDomain': true,
				'timeout': 60000,
				'type': 'GET',
				'url': this.configuration.configPath,
				'error': function(e, f, g){
					thisPlayer.throwError(new Error(thisPlayer.getLabel('configurationAccessError')), true);
				},
				'success': function(data){
					if(data.accessRestriction.passed){
						if(intendedAction){
							intendedAction();
						}
						else{
							data.configPath = thisPlayer.configuration.configPath;
							thisPlayer.init(data);
						}
					}
					else{
						delete thisPlayer.configuration.accessToken;
						thisPlayer.handleAccessRestriction(intendedAction);
					}
				}
			});
		}
		else{
			var title = this.getLabel('commonErrorTitle');
			var content = '';
			if(this.configuration.accessRestriction.additionalInformation == 'password'){
				title = this.getLabel('passwordRequiredTitle');
				content = '<label><span>' + this.getLabel('videoPasswordField') + ':</span> <input type="password" name="password" /></label>';
			}
			else if(this.configuration.accessRestriction.additionalInformation == 'token'){
				title = this.getLabel('tokenRequiredTitle');
				content = '<label><span>' + this.getLabel('videoTokenField') + ':</span> <input type="text" name="token" /></label>';
			}
			else if(this.configuration.accessRestriction.additionalInformation == 'login'){
				title = this.getLabel('loginRequiredTitle');
				content = '<label><span>' + this.getLabel('usernameField') + ':</span> <input type="text" name="username" /></label><label><span>' + this.getLabel('passwordField') + ':</span> <input type="password" name="password" /></label>';
			}
			this.createPopup('accessRestriction', false);
			$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_title', this.player).text(title);
			if(content != ''){
				$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content .sivaPlayer_scrollable', this.player).append('<form>' + content + '<label><span></span> <button>' + this.getLabel('getAccessButton') + '</button></label>' + ((this.configuration.accessRestriction.additionalInformation == 'login') ? '<label class="sivaPlayer_noAccount">' + thisPlayer.getLabel('forgottenText') + ' <a href="' + thisPlayer.configuration.videoPath.split('/sivaPlayerVideos')[0] + '/xhtml/users/recoverPassword.jsf" target="_blank" title="' + thisPlayer.getLabel('forgottenTooltip') + '">' + thisPlayer.getLabel('forgottenLink') + '</a></label><label class="sivaPlayer_noAccount">' + this.getLabel('registerText') + ' <a href="' + this.configuration.videoPath.split('/sivaPlayerVideos')[0] + '/xhtml/users/register.jsf" target="_blank" title="' + this.getLabel('registerTooltip') + '">' + this.getLabel('registerLink') + '</a></label>' : '') + '</form>');
			}
			if(this.configuration.accessRestriction.code != ''){
				$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content .sivaPlayer_scrollable', this.player).prepend('<span class="sivaPlayer_message">' + this.getLabel(this.configuration.accessRestriction.description) + '</span>');
			}
			$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content button', this.player).click(function(e){
				thisPlayer.startLoader();
				var params = $('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content form', thisPlayer.player).serialize();
				$('.sivaPlayer_accessRestrictionPopup', thisPlayer.player).remove();
				$.ajax({
					'async': true,
					'data': 'ajax=true&' + params,
					'dataType': 'JSON',
					'crossDomain': true,
					'timeout': 60000,
					'type': 'POST',
					'url': thisPlayer.configuration.configPath,
					'error': function(){
						thisPlayer.throwError(new Error(thisPlayer.getLabel('videoAccessError')), true);
					},
					'success': function(data){
						data.additionalInformation = thisPlayer.configuration.accessRestriction.additionalInformation;
						thisPlayer.configuration.accessRestriction = data;
						thisPlayer.handleAccessRestriction(intendedAction);
					}
				});
				e.preventDefault();
			});
			thisPlayer.stopLoader();
		}
	};
	
	this.handleKeygenRestriction = function(message){
		var thisPlayer = this;
		sivaPlayerStorage.get('keygen', function(keygen){
			if(!keygen){
				var title = thisPlayer.getLabel('keygenRequiredTitle');
				var content = '<label><span>' + thisPlayer.getLabel('emailField') + ':</span> <input type="text" name="email" /></label><label><span>' + thisPlayer.getLabel('codeField') + ':</span> <input type="text" name="code" /></label>';
				thisPlayer.createPopup('accessRestriction', false);
				$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_title', thisPlayer.player).text(title);
				if(content != ''){
					$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).append('<form>' + content + '<label><span></span> <button>' + thisPlayer.getLabel('getAccessButton') + '</button></label></form>');
				}
				if(message){
					$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).prepend('<span class="sivaPlayer_message">' + thisPlayer.getLabel(message) + '</span>');
				}
				$('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content button', thisPlayer.player).click(function(e){
					thisPlayer.startLoader();
					var data = {};
					data.keygenEmail = $('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content form input[name="email"]', thisPlayer.player).val();
					data.keygenCode = $('.sivaPlayer_accessRestrictionPopup .sivaPlayer_content form input[name="code"]', thisPlayer.player).val();
					$('.sivaPlayer_accessRestrictionPopup', thisPlayer.player).remove();
					if(data.keygenEmail == '' || data.keygenCode == ''){
						thisPlayer.handleKeygenRestriction('emptyFieldsError');
					}
					else if(thisPlayer.isKeygenCodeValid(data.keygenEmail, data.keygenCode)){
						sivaPlayerStorage.set('keygen', JSON.stringify(data));
						thisPlayer.handleKeygenRestriction();
					}
					else{
						thisPlayer.handleKeygenRestriction('keygenInvalidError');
					}
					e.preventDefault();
				});
				thisPlayer.stopLoader();
			}
			else{
				keygen = JSON.parse(keygen);
				thisPlayer.configuration.common.keygenEmail = keygen.keygenEmail;
				thisPlayer.configuration.common.keygenCode = keygen.keygenCode;
				thisPlayer.init(thisPlayer.configuration, true);
			}
		});
	};
	
	this.isAccessRestrictionSessionExpired = function(intendedAction){
		var result = $.parseJSON($.ajax({
			'async': false,
			'data': {'ajax': 'true', 'token': this.configuration.accessRestriction.accessToken},
			'dataType': 'JSON',
			'crossDomain': true,
			'timeout': 60000,
			'type': 'GET',
			'url': thisPlayer.configuration.videoPath + '/XML/checkSession.js',
			'error': function(){
				thisPlayer.throwError(new Error(thisPlayer.getLabel('sessionLoadingError')), true);
			}
		}).responseText);
		if(result.isStillActive == 'true'){
			return false;
		}
		this.configuration.accessRestriction = result;
		this.logKey = this.generateRandomHash(40);
		this.handleAccessRestriction(intendedAction);
		return true;
	};
	
	this.isKeygenCodeValid = function(email, code){
		var secretKey = this.configuration.common.secretKey;
		email = email.replace(/([^0-9a-zA-Z]+)/g, '').toLowerCase();
		var tmpKey = [];
		for(var i = 0; i < secretKey.length; i++){
			tmpKey[(i % code.length)] = secretKey.charAt((email.charAt(i % email.length).charCodeAt(0) + i) % secretKey.length);
		}
		var tmpKeyString = '';
		for(var i = 0; i < tmpKey.length; i++){
			tmpKeyString += tmpKey[i];
		}
		if(code == tmpKeyString){
			return true;
		}
		return false;
	};
	
	this.setNextNode = function(node, autostart){
		this.closePopups(0);
		var startTime = 0;
		if(node){
			var counter = parseInt(node.replace(/%7C/gi, '|').split('|')[1]);
			if(counter > 0){
				this.hashchangeCounter = counter;
			}
			this.hashchangeCounter++;
			var tmp = node.split(',');
			if(tmp.length > 1){
				startTime = parseInt(tmp[1].replace(/%7C/gi, '|').split('|')[0]);
			}
			node = this.removeTimestampFromFragment(node);
		}
		if(node && (node.split('-')[0] == 'select' || node == this.configuration.endScene || node == this.configuration.endScene.node)){
			this.createNodeSelectionPopup(node);
			thisPlayer.stopLoader();
		}
		else{
			if(!node){
				node = this.configuration.startScene;
			}
			this.updateHistory(node);
			this.createTopControls(node, true);
			this.setScene(node, startTime, autostart);
		}
	};
	
	this.setScene = function(scene, startTime, autostart){
		var thisPlayer = this;
		this.currentSceneEnded = false;
		this.startLoader();
		if(this.configuration.accessRestriction && this.isAccessRestrictionSessionExpired(function(){thisPlayer.setScene(scene, startTime, autostart);})){
			return;
		}
		this.currentScene = scene;
		this.sceneHistory[scene] = true;
		this.logAction('loadScene', scene, this.configuration.scenes[scene].title[this.currentLanguage].content);
		$('.sivaPlayer_bottomControls, .sivaPlayer_annotationSidebar, .sivaPlayer_annotationSidebarButton, .sivaPlayer_searchSidebar, .sivaPlayer_videoContainer .sivaPlayer_annotation, .sivaPlayer_popup, .sivaPlayer_nodeSelectionSidebar', this.player).remove();
		if($('.sivaPlayer_mainVideo', this.player).length == 0 || this.isMobile){
			$('.sivaPlayer_videoBackground', this.player).remove();
			var videoElement = '<video class="sivaPlayer_mainVideo">';
			var testElement = document.createElement("video");
			for(var i = 0; i < this.configuration.scenes[scene].files.length; i++){
				var file = this.configuration.scenes[scene].files[i];
				if(testElement.canPlayType(file.type) != ''){
					supportedFormatFound = true;
					this.configuration.supportedVideoTypes.push(file.type);
					videoElement += '<source src="' + this.configuration.videoPath + file.url[this.currentLanguage].href + "." + file.format + ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) ? '?token=' + this.configuration.accessRestriction.accessToken : '') + '" type="' + file.type + '" />';
				}
			}
			videoElement += '</video>';
			if(this.configuration.supportedVideoTypes.length == 0){;
				this.throwError(new Error(this.getLabel('noSupportedVideoFormatError')), false);
			}
			$(this.player).append('<div class="sivaPlayer_videoBackground"><div class="sivaPlayer_overlayButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 300 300"><circle cx="150" cy="150" r="150"/><g class="sivaPlayer_playButton" title="' + this.getLabel('playTooltip') + '"><polygon points="108.5,82.767 108.5,225.856 232.5,154.31 "/></g><g class="sivaPlayer_replayButton" title="' + this.getLabel('replayTooltip') + '"><path d="M149.539,64.941c-15.144,0-29.366,3.874-41.87,10.596L81.087,53.065l-12.647,95.786l81.024-37.991 l-24.24-20.486c7.577-2.896,15.741-4.557,24.315-4.557c38.209,0,69.193,31.392,69.193,70.128c0,38.73-30.984,70.12-69.193,70.12 c-32.647,0-59.939-22.94-67.226-53.769l-21.645-4.021c5.953,44.404,43.414,78.658,88.87,78.658 c49.592,0,89.792-40.736,89.792-90.989C239.331,105.678,199.131,64.941,149.539,64.941z"/></g></svg></div><div class="sivaPlayer_videoContainer">' + videoElement + '</div></div>');
			
			$('.sivaPlayer_videoBackground', thisPlayer.player).click(function(){
				var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player);
				var prevPaused = false;
				if(videoElement.length > 0){
					prevPaused = videoElement[0].paused;
				}
				thisPlayer.tidyPlayer();
				if(videoElement.length > 0 && prevPaused == videoElement[0].paused){
					if(!videoElement[0].paused){
						thisPlayer.pauseVideo();
						thisPlayer.logAction('clickVideo', 'pause', '');
					}
					else{
						if(!thisPlayer.currentSceneEnded){
							thisPlayer.logAction('clickVideo', 'play', '');
						}
						else{
							thisPlayer.logAction('clickVideo', 'replay', '');
						}
						thisPlayer.playVideo();
					}
				}
			})
			.mouseover(function(){
				$(this).addClass('sivaPlayer_hover');
			})
			.mouseout(function(){
				$(this).removeClass('sivaPlayer_hover');
			});
			
			var sources = $('.sivaPlayer_mainVideo source', this.player);
			$(sources[(sources.length - 1)]).on('error', function(){
				thisPlayer.throwError(new Error(thisPlayer.getLabel('videoStartupError')), true);
			});
		}
		else{
			$('.sivaPlayer_videoContainer', this.player).removeAttr('style');
			for(var i = 0; i < this.configuration.scenes[scene].files.length; i++){
				var file = this.configuration.scenes[scene].files[i];
				if($.inArray(file.type, this.configuration.supportedVideoTypes) > -1){
					$('.sivaPlayer_mainVideo', this.player).attr('src', this.configuration.videoPath + file.url[this.currentLanguage].href + "." + file.format + ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) ? '?token=' + this.configuration.accessRestriction.accessToken : ''));
					break;
				}
			}
		}

		$('.sivaPlayer_mainVideo', this.player).unbind()
		.bind('loadeddata', function(){
			thisPlayer.configuration.ratio = this.videoWidth / this.videoHeight;
			thisPlayer.onLoadedData(scene, autostart, startTime);
		})
		.bind('loadstart', function(){
			if(thisPlayer.isMobile){
				$('.sivaPlayer_topControls', thisPlayer.player).remove();
				var helper = $('<div class="sivaPlayer_mobileHelper"></div>')
				.click(function(){
					thisPlayer.configuration.ratio = this.videoWidth / this.videoHeight;
					thisPlayer.playVideo();
					thisPlayer.onLoadedData(scene, autostart, startTime);
				});
				$(thisPlayer.player).append(helper);
				thisPlayer.stopLoader();
			}
		})
		.bind('play', function(){
			$('.sivaPlayer_controls .sivaPlayer_playButton, .sivaPlayer_controls .sivaPlayer_replayButton, .sivaPlayer_videoBackground>.sivaPlayer_overlayButton', thisPlayer.player).hide(0);
			$('.sivaPlayer_controls .sivaPlayer_pauseButton', thisPlayer.player).show(0);
		})
		.bind('pause', function(){
			if(!thisPlayer.currentSceneEnded){
				$('.sivaPlayer_controls .sivaPlayer_playButton, .sivaPlayer_videoBackground>.sivaPlayer_overlayButton, .sivaPlayer_videoBackground>.sivaPlayer_overlayButton .sivaPlayer_playButton', thisPlayer.player).show(0);
				$('.sivaPlayer_controls .sivaPlayer_replayButton, .sivaPlayer_videoBackground>.sivaPlayer_overlayButton .sivaPlayer_replayButton, .sivaPlayer_controls .sivaPlayer_pauseButton', thisPlayer.player).hide(0);
			}
		})
		.bind('volumechange', function(){
			if(thisPlayer.volume == 0){
				$('.sivaPlayer_controls .sivaPlayer_muteButton', this.player).css('display', 'inline');
				$('.sivaPlayer_controls .sivaPlayer_unmuteButton', this.player).css('display', 'none');
			}
			else{
				$('.sivaPlayer_controls .sivaPlayer_muteButton', this.player).css('display', 'none');
				$('.sivaPlayer_controls .sivaPlayer_unmuteButton', this.player).css('display', 'inline');
			}
		})
		.bind('ended', function(){
			thisPlayer.onSceneEnd(this);
		})
		.bind('error', function(e){
			return;
			thisPlayer.throwError(new Error(thisPlayer.getLabel('videoStartupError')), true);
		});
		
		$('.sivaPlayer_videoBackground')
		.bind('swipeleft swipeup swipedown swiperight', function(){
			thisPlayer.fadeInFadeOutControls(3);
		});
	};
	
	this.onLoadedData = function(scene, autostart, startTime){
		$('.sivaPlayer_mobileHelper', this.player).remove();
		if(this.active){
			if($('.sivaPlayer_topControls', this.player).length == 0){
				this.createTopControls(scene, true);
			}
			this.setCurrentSceneTime(startTime);
			this.stopLoader();
			var amountSitebarAnnotations = 0;
			var amountSubtitles = 0;
			var amountDisableableAnnotations = 0;
			for(var i = 0; i < this.configuration.globalAnnotations.length; i++){
				var annotation = this.configuration.annotations[(this.configuration.globalAnnotations[i])];
				if(annotation.type == 'subTitle'){
					amountSubtitles++;
				}
				if(annotation.isSidebarAnnotation){
					amountSitebarAnnotations++;
				}
				if(annotation.disableable){
					amountDisableableAnnotations++;
				}
			}
			for(var i = 0; i < this.configuration.scenes[this.currentScene].annotations.length; i++){
				var trigger = this.configuration.scenes[this.currentScene].annotations[i];
				var annotation = this.configuration.annotations[trigger.annotationId];
				if(annotation.type == 'subTitle'){
					amountSubtitles++;
				}
				if(annotation.isSidebarAnnotation){
					amountSitebarAnnotations++;
				}
				if(annotation.target && thisPlayer.configuration.annotations[annotation.target].isSidebarAnnotation){
					amountSitebarAnnotations++;
				}
				if(annotation.disableable){
					amountDisableableAnnotations++;
				}
			}
			this.createControls(amountSitebarAnnotations, amountSubtitles, amountDisableableAnnotations);
			if(amountSitebarAnnotations > 0){
				this.createAnnotationSidebar();
			}
			this.setVolume(true, true);
			this.createAnnotations();
			this.setProportions();
			if(amountSitebarAnnotations > 0 && this.isAnnotationSidebarVisible){
				this.slideInAnnotationSidebar();
			}
			if(autostart){
				this.playVideo();
			}
			else{
				this.pauseVideo();
			}
		}
	};
	
	this.onSceneEnd = function(videoElement){
		if(this.currentSceneEnded)
			return;
		videoElement.pause();
		$('.sivaPlayer_controls .sivaPlayer_playButton, .sivaPlayer_controls .sivaPlayer_pauseButton, .sivaPlayer_videoBackground > .sivaPlayer_overlayButton .sivaPlayer_playButton', thisPlayer.player).hide(0);
		$('.sivaPlayer_videoBackground > .sivaPlayer_overlayButton, .sivaPlayer_videoBackground > .sivaPlayer_overlayButton .sivaPlayer_replayButton, .sivaPlayer_controls .sivaPlayer_replayButton', thisPlayer.player).show(0);
		this.currentSceneEnded = true;
		if(this.currentSceneTime != videoElement.duration){
			this.setCurrentSceneTime(videoElement.duration);
			this.updateTimeline(true);
		}
		if($('.sivaPlayer_nodeSelectionPopup', this.player).length == 0){
			var node = this.configuration.scenes[this.currentScene].next;
			if(node.split('-')[0] == 'random'){
				node = this.getRandomSelection(node);
			}
			var next = {};
			next[this.arrayPosition] = node + '|' + this.hashchangeCounter;
			$.bbq.pushState(next);
		}
	};
	
	this.updateHistory = function(scene){
		var tmp = [];
		for(var i = 0; i < this.history.length && this.history[i] != scene; i++){
			tmp.push(this.history[i]);			
		}
		tmp.push(scene);
		this.history = tmp;
	};
	
	this.playVideo = function(){
		this.preventControlsFadeOut = false;
		for(var i in this.configuration.annotations){
			if((this.configuration.annotations[i].type == 'video' || this.configuration.annotations[i].type == 'audio') && !(thisPlayer.configuration.annotations[i].playsSynced || thisPlayer.configuration.annotations[i].playsFree)){
				var found = $('.sivaPlayer_annotation[class*="' + i + '_"] video, .sivaPlayer_annotation[class*="' + i + '_"] audio', thisPlayer.player);
				if(found.length > 0){
					found[0].pause();
				}
			}
		}
		var videoElements = $('.sivaPlayer_mainVideo', this.player);
		if(videoElements.length > 0){
			if(this.currentSceneEnded){
				this.setCurrentSceneTime(0);
				this.currentSceneEnded = false;
			}
			videoElements[0].play();
		}
		this.clearPlayedBefore();
		this.updateTimeline(false);
		$('.sivaPlayer_nextButton', this.player).removeClass('sivaPlayer_disabled');
		$('.sivaPlayer_topControls .sivaPlayer_sceneTitle span', this.player).fadeIn(500);
		this.fadeInFadeOutControls(1);
	};
	
	this.pauseVideo = function(){
		var videoElements = $('.sivaPlayer_mainVideo', this.player);
		if(videoElements.length > 0){
			videoElements[0].pause();
		}
		this.preventControlsFadeOut = true;
		this.fadeInFadeOutControls(3);
	};
	
	this.setPlayedBefore = function(exceptVideo){
		var thisPlayer = this;
		$.each($('video, audio', this.player), function(){
			if(!this.paused && this != exceptVideo){
				thisPlayer.playedBefore.push(this);
				if($(this).hasClass('sivaPlayer_mainVideo')){
					thisPlayer.pauseVideo();
				}
				else{
					this.pause();
				}
			}
		});
	};
	
	this.restorePlayedBefore = function(){
		for(var i = 0; i < this.playedBefore.length; i++){
			if($(this.playedBefore[i]).hasClass('sivaPlayer_mainVideo')){
				this.playVideo();
			}
			else{
				this.playedBefore[i].play();
			}
		}
		this.clearPlayedBefore();
	};
	
	this.clearPlayedBefore = function(){
		this.playedBefore = [];
	};
	
	this.setVolume = function(isSetByUser, hideVolumeControl){
		var thisPlayer = this;
		$.each($('video, audio', this.player), function(){
			this.volume = thisPlayer.volume;
		});
		$('.sivaPlayer_volumeControlVolumeBar span', this.player).css('height', parseInt(this.volume * 100) + '%');
		if(isSetByUser){
			this.previousVolume = -1;
			sivaPlayerStorage.set('volume', JSON.stringify({
				'percent': this.volume,
				'time': (new Date()).getTime()
			}));
		}
		this.volumeSetByUser = isSetByUser;
		if(hideVolumeControl){
			$('.sivaPlayer_volumeControl', this.Player).remove();
		}
	};
	
	this.createControls = function(amountSitebarAnnotations, amountSubtitles, amountDisableableAnnotations){
		var thisPlayer = this;		
		var controls = $('<tr></tr>');
		controls.append('<td class="sivaPlayer_spacer"></td>');
		var playPauseButton = $('<td class="sivaPlayer_button sivaPlayer_playPauseButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_playButton" title="' + this.getLabel('playTooltip') + '"><polygon points="2,0 2,30 28,15"/></g><g class="sivaPlayer_pauseButton" title="' + this.getLabel('pauseTooltip') + '"><rect x="2" width="10.4" height="30"/><rect x="17.6" width="10.4" height="30"/></g><g class="sivaPlayer_replayButton" title="' + this.getLabel('replayTooltip') + '"><path d="M14.928,1.838c-2.375,0-4.605,0.599-6.566,1.64L4.192,0L2.209,14.822l12.707-5.878l-3.802-3.17 c1.188-0.448,2.469-0.706,3.813-0.706c5.992,0,10.852,4.857,10.852,10.852c0,5.994-4.859,10.851-10.852,10.851 c-5.12,0-9.401-3.55-10.542-8.32l-3.395-0.622C1.924,24.699,7.799,30,14.928,30c7.777,0,14.082-6.304,14.082-14.081 C29.01,8.142,22.705,1.838,14.928,1.838z"/></g></svg></td>')
		.click(function(){
			var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player);
			var prevPaused = false;
			if(videoElement.length > 0){
				prevPaused = videoElement[0].paused;
			}
			thisPlayer.tidyPlayer();
			if(videoElement.length > 0 && prevPaused == videoElement[0].paused){
				if(!videoElement[0].paused){
					thisPlayer.logAction('useButton', 'pause', '');
					thisPlayer.pauseVideo();
				}
				else{
					if(!thisPlayer.currentSceneEnded){
						thisPlayer.logAction('useButton', 'play', '');
					}
					else{
						thisPlayer.logAction('useButton', 'replay', '');
					}
					thisPlayer.playVideo();
				}
			}
		});
		controls.append(playPauseButton);
		var duration = this.formatTime($('.sivaPlayer_mainVideo', this.player)[0].duration);
		controls.append('<td class="sivaPlayer_spacer"></td>');
		controls.append('<td class="sivaPlayer_timeline"><span class="sivaPlayer_timelineProgress"><span class="sivaPlayer_timelineProgressBar"><span></span></span></span><span><span class="sivaPlayer_timelineCurrentTime">00:00</span><span class="sivaPlayer_timelineDuration">' + duration + '</span></span></td>');
		$('.sivaPlayer_timelineProgress', controls)
		.bind('mousemove', function(e){
			var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player)[0];
			if($('.sivaPlayer_controls .sivaPlayer_timelineUpdate', thisPlayer.player).length == 0){
				$(this).append('<span class="sivaPlayer_timelineUpdate"><span></span></span><span class="sivaPlayer_timlineUpdateSelectedTime"></span>');
			}
			var offset = $(this).offset();
			var width = (e.pageX - offset.left);
			if(width + 1 > $(this).width()){
				width = $(this).width() - 1;
			}
			$('.sivaPlayer_controls .sivaPlayer_timelineUpdate span', thisPlayer.player).css('width', width + 'px');
			var percentage = (e.pageX - $(this).offset().left) / $(this).width();
			var duration = videoElement.duration;
			var timeSelectionWidth = $('.sivaPlayer_controls .sivaPlayer_timlineUpdateSelectedTime', thisPlayer.player).width();
			var timeSelectionLeft = parseInt(e.pageX - $(this).offset().left - timeSelectionWidth / 2);
			if(timeSelectionLeft < 0){
				timeSelectionLeft = 0;
			}	
			else if(timeSelectionLeft + timeSelectionWidth > $(this).width()){
				timeSelectionLeft = $(this).width() - timeSelectionWidth;
			}
			$('.sivaPlayer_controls .sivaPlayer_timlineUpdateSelectedTime', thisPlayer.player).text(thisPlayer.formatTime(duration * percentage))
				.css('left', timeSelectionLeft + 'px');
		})
		.bind('mouseleave', function(){
			$('.sivaPlayer_timelineUpdate, .sivaPlayer_timlineUpdateSelectedTime', this).remove();
		})
		.click(function(e){
			thisPlayer.tidyPlayer();
			var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player)[0];
			if(videoElement.paused){
				$('.sivaPlayer_controls .sivaPlayer_playButton, .sivaPlayer_videoBackground .sivaPlayer_overlayButton, .sivaPlayer_videoBackground .sivaPlayer_overlayButton .sivaPlayer_playButton', thisPlayer.player).show(0);
				$('.sivaPlayer_controls .sivaPlayer_replayButton, .sivaPlayer_videoBackground .sivaPlayer_overlayButton .sivaPlayer_replayButton, .sivaPlayer_controls .sivaPlayer_pauseButton', thisPlayer.player).hide(0);
			}
			var percentage = (e.pageX - $(this).offset().left) / $(this).width();
			var duration = videoElement.duration;
			thisPlayer.logAction('selectTime', '', (duration * percentage) + '');
			thisPlayer.currentSceneEnded = false;
			$('.sivaPlayer_topControls .sivaPlayer_nextButton', thisPlayer.player).removeClass('sivaPlayer_disabled');
			thisPlayer.setCurrentSceneTime(duration * percentage);
			thisPlayer.updateTimeline(true);
		});
		controls.append('<td class="sivaPlayer_spacer"></td>');
		var sound = $('<td class="sivaPlayer_button sivaPlayer_volumeButton"><svg title="' + this.getLabel('volumeTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_muteButton"><path d="M26.533,2.611L0,26.595l1.427,1.712L27.961,4.322L26.533,2.611z M18.289,15.655c-0.045,6.851-0.784,12.307-1.695,12.307c-0.75,0-1.777-3.695-2.161-8.811l-4.978,4.494 c2.116,3.11,4.46,6.354,5.05,6.354h2.472c1.634,0,2.861-6.75,2.861-15.033l-0.006-0.705L18.289,15.655z M4.294,20.623h0.195 l10.383-9.378c-0.139-0.134-0.289-0.25-0.445-0.345c0.384-5.15,1.412-8.861,2.167-8.861c0.623,0,1.162,2.539,1.462,6.328 l1.389-1.256C18.972,2.839,18.105,0,17.044,0h-2.539c-0.8,0-4.395,5.64-6.694,9.378H4.294c-0.895,0-1.623,2.495-1.623,5.623 C2.672,18.128,3.4,20.623,4.294,20.623z"/></g><g class="sivaPlayer_unmuteButton"><path d="M26.881,4.771L25.27,5.718c1.268,2.919,1.939,6.145,1.939,9.357 s-0.672,6.438-1.939,9.357l1.611,0.947c1.377-3.221,2.1-6.778,2.1-10.305C28.98,11.543,28.258,7.99,26.881,4.771z M24.525,9.226 l-1.717,0.839c0.635,1.587,1.127,3.309,1.127,5.03s-0.492,3.469-1.127,5.056l1.639,0.569c0.762-1.875,1.268-3.692,1.268-5.708 C25.715,13.033,25.273,11.088,24.525,9.226z M20.527,13.424c0.494,1.07,0.486,2.454-0.021,3.796l1.496,0.565 c0.658-1.741,0.65-3.574-0.02-5.03L20.527,13.424z M16.999,0H14.45c-0.799,0-4.408,5.638-6.713,9.375H6.108h-1.51h-0.39 c-0.894,0-1.623,2.497-1.623,5.625c0,3.129,0.729,5.625,1.623,5.625h1.899v0.021h1.268C9.694,24.16,13.664,30,14.45,30h2.479 c1.639,0,2.871-6.75,2.871-15.032S18.633,0,16.999,0z M16.547,27.96c-0.756,0-1.786-3.716-2.173-8.858 c1.052-0.644,1.797-2.235,1.797-4.102c0-1.871-0.745-3.458-1.797-4.102c0.387-5.147,1.417-8.857,2.173-8.857 c0.943,0,1.705,5.801,1.705,12.959C18.252,22.152,17.49,27.96,16.547,27.96z"/></g></svg></td>');
		$('svg', sound).click(function(){
			thisPlayer.tidyPlayer();
			if($('.sivaPlayer_controls .sivaPlayer_volumeControl', this.player).length == 0){
				thisPlayer.logAction('useButton', 'volume', '');
				thisPlayer.createVolumeControl();
			}
			else{
				$('.sivaPlayer_controls .sivaPlayer_volumeControl', this.player).remove();
			}
		});
		controls.append(sound);
		if(this.volume == 0){
			$('.sivaPlayer_muteButton', controls).css('display', 'inline');
			$('.sivaPlayer_unmuteButton', controls).css('display', 'none');
		}
		if(amountSubtitles > 0){
			controls.append('<td class="sivaPlayer_spacer"></td>');
			var subTitleButton = $('<td class="sivaPlayer_button"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_subtitleOnButton" title="' + this.getLabel('subtitleOnTooltip') + '"><path d="M0,3v23h30V3H0z M5,17h20v2H5V17z M28,23H2v-2h26V23z"/></g><g class="sivaPlayer_subtitleOffButton" title="' + this.getLabel('subtitleOffTooltip') + '"><g><polygon points="16.306,17 25,17 25,19 14.093,19 11.88,21 28,21 28,23 9.667,23 6.349,26 29,26 29,5.621"/><polygon points="2,22.227 2,21 3.357,21 5.57,19 5,19 5,17 7.783,17 23.271,3 1,3 1,23.035"/></g><path d="M27.553,1.486l1.428,1.711L2.447,27.182L1.02,25.47L27.553,1.486z"/></g></svg></td>')
			.click(function(){
				thisPlayer.tidyPlayer();
				thisPlayer.logAction('useButton', 'subTitle', ((thisPlayer.isSubTitleVisible) ? 'hide' : 'show'));
				thisPlayer.isSubTitleVisible = !thisPlayer.isSubTitleVisible;
				thisPlayer.setSubtitleButton();
				thisPlayer.updateAnnotations();
			});
			controls.append(subTitleButton);
		}
		if(amountDisableableAnnotations > 0){
			controls.append('<td class="sivaPlayer_spacer"></td>');
			var annotationButton = $('<td class="sivaPlayer_button"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_annotationOnButton" title="' + this.getLabel('annotationOnTooltip') + '"><g><path d="M1,3v23h28V3H1z M28,25H2V4h26V25z"/><g><rect x="4" y="6" width="16" height="4"/></g><g><rect x="17" y="12" width="10" height="10"/></g><ellipse cx="8.75" cy="17.416" rx="4.448" ry="3.813"/></g></g><g class="sivaPlayer_annotationOffButton" title="' + this.getLabel('annotationOffTooltip') + '"><path d="M27.395,2.279l1.428,1.711L2.29,27.975l-1.428-1.712L27.395,2.279z"/><g><polygon points="2,22.768 2,4 22.683,4 23.785,3 1,3 1,23.675"/><polygon points="28,6.973 28,25 8.027,25 6.919,26 29,26 29,6.07"/></g><path d="M11.492,14.437c-0.758-0.513-1.702-0.832-2.742-0.832c-2.456,0-4.448,1.707-4.448,3.812c0,0.97,0.436,1.845,1.132,2.518L11.492,14.437z"/><g><polygon points="20,6.435 20,6 4,6 4,10 16.621,10"/></g><g><polygon points="17,16.784 17,22 27,22 27,12 22.688,12"/></g></g></svg></td>')
			.click(function(){
				thisPlayer.tidyPlayer();
				thisPlayer.logAction('useButton', 'annotation', ((thisPlayer.isSubTitleVisible) ? 'hide' : 'show'));
				thisPlayer.isAnnotationVisible = !thisPlayer.isAnnotationVisible;
				thisPlayer.setAnnotationButton();
				thisPlayer.updateAnnotations();
			});
			controls.append(annotationButton);
		}
		controls.append('<td class="sivaPlayer_spacer"></td>');
		if(this.configuration.common.collaboration){
			var collaborationButton = $('<td class="sivaPlayer_button"><svg title="' + this.getLabel('collaborationTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><path d="M26.224,0H3.777C1.691,0,0,1.525,0,3.408v13.292C0,18.581,1.691,20,3.777,20H9L6,30l10.125-10h10.099 C28.31,20,30,18.581,30,16.699V3.408C30,1.525,28.31,0,26.224,0z M5.75,15C5.336,15,5,14.664,5,14.25s0.336-0.75,0.75-0.75 s0.75,0.336,0.75,0.75S6.164,15,5.75,15z M9.75,15C9.335,15,9,14.664,9,14.25c0-0.415,0.335-0.75,0.75-0.75s0.75,0.335,0.75,0.75 C10.5,14.664,10.165,15,9.75,15z M13.75,15C13.335,15,13,14.664,13,14.25c0-0.415,0.335-0.75,0.75-0.75s0.75,0.335,0.75,0.75 C14.5,14.664,14.165,15,13.75,15z"/></svg></td>')
			.click(function(){
				thisPlayer.tidyPlayer();
				thisPlayer.logAction('useButton', 'collaboration', '');
				thisPlayer.logAction('openCollaboration', '', '');
				thisPlayer.setPlayedBefore();					
				thisPlayer.createPopup('collaboration', true);
				$('.sivaPlayer_collaborationPopup .sivaPlayer_closeButton div', this.player).click(function(){
					thisPlayer.logAction('closeCollaboration', '', '');
				});				
				$('.sivaPlayer_collaborationPopup .sivaPlayer_title', thisPlayer.player).append(thisPlayer.getLabel('collaborationTitle'));
				$('.sivaPlayer_collaborationPopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).append('<p>' + thisPlayer.getLabel('collaborationText') + '</p><div class="sivaPlayer_sceneList"></div>');
				var button = $('<a title="' + thisPlayer.getLabel('collaborationProceedTooltip') + '">' + thisPlayer.getLabel('collaborationProceedLink') + '</a>').click(function(e){
					$('.sivaPlayer_collaborationPopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).empty();
					e.preventDefault();
				});
				$('.sivaPlayer_collaborationPopup .sivaPlayer_content .sivaPlayer_sceneList', thisPlayer.player).append(button);
				button = $('<a title="' + thisPlayer.getLabel('collaborationAbortTooltip') + '">' + thisPlayer.getLabel('collaborationAbortLink') + '</a>').click(function(e){
					thisPlayer.closePopups(600);
					e.preventDefault();
				});
				$('.sivaPlayer_collaborationPopup .sivaPlayer_content .sivaPlayer_sceneList', thisPlayer.player).append(button);
			}); 
			controls.append(collaborationButton);
			controls.append('<td class="sivaPlayer_spacer"></td>');
		}
		var settings = $('<td class="sivaPlayer_button"><svg title="' + this.getLabel('settingsTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><path d="M28.97,20.526L30,15.831l-4.81-2.11l-0.02,0.09c-0.038-0.321-0.088-0.642-0.157-0.964c-0.034-0.156-0.083-0.305-0.124-0.458 l0.193,0.301l3.782-3.644l-2.591-4.049l-4.893,1.91l0.12,0.188c-0.375-0.309-0.772-0.587-1.186-0.839l0.112,0.024l0.097-5.251 L15.83,0l-2.109,4.811l0.09,0.02c-0.321,0.038-0.642,0.088-0.964,0.157c-0.157,0.034-0.306,0.083-0.459,0.124l0.303-0.194 L9.047,1.133L4.998,3.726l1.909,4.893l0.189-0.121C6.788,8.873,6.51,9.27,6.257,9.684l0.024-0.111L1.031,9.475L0,14.17l4.811,2.109 l0.02-0.09c0.038,0.321,0.088,0.642,0.157,0.964c0.017,0.076,0.042,0.147,0.06,0.222l-3.751,3.613l2.592,4.048l4.892-1.908 c0.291,0.222,0.593,0.425,0.905,0.615l-0.112-0.024L9.474,28.97L14.169,30l2.11-4.81l-0.091-0.02 c0.321-0.038,0.643-0.087,0.965-0.157c0.075-0.017,0.146-0.042,0.221-0.06l3.613,3.751l4.049-2.591l-1.909-4.892 c0.222-0.29,0.426-0.593,0.616-0.906l-0.024,0.112L28.97,20.526z M16.039,19.832c-2.668,0.575-5.296-1.124-5.871-3.793 c-0.573-2.667,1.125-5.296,3.793-5.869c2.669-0.574,5.296,1.124,5.871,3.792C20.405,16.629,18.707,19.258,16.039,19.832z"/></svg></td>')
		.click(function(){
			thisPlayer.tidyPlayer();
			thisPlayer.logAction('useButton', 'settings', '');
			thisPlayer.setPlayedBefore();
			thisPlayer.createSettingsPopup();
		});
		controls.append(settings);
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_rightOuterSpacer"></td>');
		$(this.player).append($('<table class="sivaPlayer_bottomControls sivaPlayer_controls sivaPlayer_transition"></table').append(controls));
		this.setSubtitleButton();
		this.setAnnotationButton();
		this.updateTimeline(true);
		
		$('.sivaPlayer_controls', this.player).hover(function(){
			thisPlayer.preventControlsFadeOut = true;
		}, function(){
			var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player);
			thisPlayer.preventControlsFadeOut = (videoElement.length > 0 && videoElement[0].paused);
		});
		this.fadeInFadeOutControls(3);
	};
	
	this.createTopControls = function(node, isScene){
		if(!this.configuration.scenes[node] && isScene){
			this.throwError(new Error(this.getLabel('unknownSceneOrNode')), false);
		}	
		$('.sivaPlayer_topControls', this.player).remove();
		var controls = $('<tr></tr>');
		controls.append('<td class="sivaPlayer_spacer"></td>');
		controls.append('<td class="sivaPlayer_button sivaPlayer_tableOfContentsButton"><svg title="' + this.getLabel('tableOfContentsTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><rect y="1" width="29" height="4"/><rect y="7" width="25" height="4"/><rect y="13" width="29" height="4"/><rect y="25" width="4" height="4"/><rect x="6" y="25" width="4" height="4"/><rect x="12" y="25" width="4" height="4"/><rect y="19" width="28" height="4"/></svg></td>');
		controls.append('<td class="sivaPlayer_spacer"></td>');
		controls.append('<td class="sivaPlayer_videoTitle">' + ((this.configuration.tableOfContents && this.configuration.tableOfContents.title[this.currentLanguage].content) ? this.configuration.tableOfContents.title[this.currentLanguage].content : this.configuration.videoTitle[this.currentLanguage].content) + '</td>');
		$('.sivaPlayer_tableOfContentsButton, .sivaPlayer_videoTitle', controls).click(function(){
			thisPlayer.tidyPlayer();
			var popup = $('.sivaPlayer_tableOfContentsPopup', thisPlayer.player);
			if(popup.length == 0){
				thisPlayer.setPlayedBefore();
				thisPlayer.logAction('openTableOfContents', '', '');
				thisPlayer.createTableOfContentsPopup(node);
			}
			else{
				thisPlayer.logAction('closeTableOfContents', '', '');
				thisPlayer.closePopups(600);
			}
		});
		controls.append('<td class="sivaPlayer_spacer"></td>');
		var prevButton = $('<td class="sivaPlayer_prevButton sivaPlayer_button ' + ((isScene) ? '' : 'sivaPlayer_disabled') + '"><svg title="' + this.getLabel('backTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><polygon points="30,0 30,30 4,15 "/><rect width="4" height="30"/></svg></td>')
		.click(function(){
			thisPlayer.tidyPlayer();
			if((thisPlayer.history.length > 1 || thisPlayer.currentSceneTime > 5) && !$(this).hasClass('sivaPlayer_disabled')){
				thisPlayer.pauseVideo();
				thisPlayer.logAction('useButton', 'back', '');
				var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player);
				if(videoElement.length > 0 && videoElement[0].currentTime > 5){
					thisPlayer.setCurrentSceneTime(0, false);
					thisPlayer.playVideo();
				}
				else{
					if(isScene){
						thisPlayer.history.pop();
					}
					var prev = {};
					var sceneId = thisPlayer.history.pop();
					if(sceneId == thisPlayer.configuration.startScene)
						$.bbq.removeState(thisPlayer.arrayPosition);
					else{
						prev[thisPlayer.arrayPosition] = sceneId;
						$.bbq.pushState(prev);
					}
				}
			}
		});
		controls.append(prevButton);
		controls.append('<td class="sivaPlayer_spacer"></td>');
		controls.append('<td class="sivaPlayer_sceneTitle"><span>' + ((isScene) ? this.configuration.scenes[node].title[this.currentLanguage].content : '') + '</span></td>');
		controls.append('<td class="sivaPlayer_spacer"></td>');
		var nextButton = $('<td class="sivaPlayer_nextButton sivaPlayer_button ' + ((isScene) ? '' : 'sivaPlayer_disabled') + '"><svg title="' + this.getLabel('forwardTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><polygon points="0,0 0,30 26,15 "/><rect x="26" width="4" height="30"/></svg></td>')
		.click(function(){
			thisPlayer.tidyPlayer();
			if(isScene && !$(this).hasClass('sivaPlayer_disabled')){	
				thisPlayer.logAction('useButton', 'next', '');
				var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player)[0];
				videoElement.play();
				thisPlayer.setCurrentSceneTime(videoElement.duration - 0.5);
				thisPlayer.updateTimeline(true);
			}
		});
		controls.append(nextButton);
		controls.append('<td class="sivaPlayer_spacer"></td>');
		controls.append('<td></td>');
		if(!Modernizr.fullscreen){
			controls.append('<td class="sivaPlayer_spacer"></td>');
			controls.append('<td class="sivaPlayer_button"></td>');		
		}
		var searchButton = $('<td class="sivaPlayer_button"><svg title="' + this.getLabel('searchTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_searchOffButton"><path d="M28.431,1.458l1.53,1.833L1.53,28.991L0,27.157L28.431,1.458z"/><g><path d="M23.986,19.654c-0.498-0.497-1.397-0.449-2.303,0.021l-2.466-2.468c0.802-1.047,1.385-2.208,1.749-3.421l-8.224,7.431 c1.588-0.308,3.121-0.972,4.466-2.001l2.467,2.468c-0.471,0.903-0.518,1.804-0.021,2.302l5.657,5.655 c0.714,0.716,2.263,0.323,3.456-0.871c1.197-1.194,1.59-2.745,0.876-3.46L23.986,19.654z"/><path d="M5.532,20.089l1.792-1.632c-0.941-0.411-1.825-0.994-2.595-1.764c-3.303-3.306-3.306-8.659,0-11.963 c3.303-3.303,8.66-3.303,11.966,0c0.969,0.968,1.645,2.116,2.046,3.334l1.791-1.631c-0.521-1.196-1.267-2.318-2.245-3.297 c-4.184-4.182-10.966-4.182-15.15,0c-4.183,4.185-4.183,10.966,0,15.15C3.865,19.015,4.673,19.614,5.532,20.089z"/></g></g><g class="sivaPlayer_searchOnButton"><path d="M28.768,28.771c1.197-1.194,1.59-2.745,0.876-3.46l-5.657-5.656c-0.498-0.497-1.397-0.449-2.303,0.021 l-2.466-2.468c3.216-4.199,2.912-10.228-0.931-14.072c-4.184-4.182-10.966-4.182-15.15,0c-4.183,4.185-4.183,10.966,0,15.15 c3.843,3.842,9.872,4.146,14.072,0.931l2.467,2.468c-0.471,0.903-0.518,1.804-0.021,2.302l5.657,5.655 C26.025,30.357,27.574,29.965,28.768,28.771z M16.695,16.693c-3.308,3.303-8.663,3.303-11.966,0c-3.303-3.306-3.306-8.659,0-11.963 c3.303-3.303,8.66-3.303,11.966,0C19.998,8.032,19.998,13.387,16.695,16.693z" /></g></svg></td>')
		.click(function(){
			thisPlayer.tidyPlayer();
			var sidebar = $('.sivaPlayer_searchSidebar', thisPlayer.player);
			if(sidebar.length == 0){
				thisPlayer.setPlayedBefore();
				thisPlayer.logAction('openSearchArea', '', '');
				thisPlayer.createSearchSidebar();
				thisPlayer.setSearchButton();
			}
			else{
				thisPlayer.logAction('closeSearchArea', '', '');
			}
		});
		controls.append(searchButton);
		if(this.configuration.common && this.configuration.common.log && (this.configuration.accessRestriction || this.configuration.common.logPath)){
			controls.append('<td class="sivaPlayer_spacer"></td>');
			var stats = $('<td class="sivaPlayer_button sivaPlayer_statsButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30"><path d="M0,1.73V29h30V1.73H0z M28.269,25.923H1.731V9.115h26.538V25.923z"/><g><line fill="none" stroke-miterlimit="10" x1="4.125" y1="12.625" x2="25.874" y2="12.625"/><line fill="none" stroke-miterlimit="10" x1="4.125" y1="17.375" x2="25.875" y2="17.375"/><line fill="none" stroke-miterlimit="10" x1="4.125" y1="22.125" x2="25.875" y2="22.125"/><line fill="none" stroke-miterlimit="10" x1="17.875" y1="9.875" x2="17.875" y2="25.124"/><line fill="none" stroke-miterlimit="10" x1="12.875" y1="9.875" x2="12.875" y2="25.125"/><line fill="none" stroke-miterlimit="10" x1="7.875" y1="9.875" x2="7.875" y2="25.125"/><line fill="none" stroke-miterlimit="10" x1="22.875" y1="9.875" x2="22.875" y2="25.124"/></g></svg></td>')
			.click(function(){
				thisPlayer.startLoader();
				thisPlayer.tidyPlayer();
				thisPlayer.logAction('useButton', 'stats', '');
				thisPlayer.setPlayedBefore();
				thisPlayer.logAction('openStats', '', '');
				thisPlayer.createPopup('stats', true);
				$('.sivaPlayer_statsPopup .sivaPlayer_closeButton div', this.player).click(function(){
					thisPlayer.logAction('closeStats', '', '');
				});
				$('.sivaPlayer_statsPopup .sivaPlayer_title', thisPlayer.player).text(thisPlayer.getLabel('stats'));
				$('.sivaPlayer_loader .sivaPlayer_logo', thisPlayer.player).show(0);
				$('.sivaPlayer_loader .sivaPlayer_text', thisPlayer.player).text(thisPlayer.getLabel('syncing'));
				sivaPlayerClearLog(function(message1, message2){
					thisPlayer.stopLoader();
					$('.sivaPlayer_statsPopup .sivaPlayer_message', thisPlayer.player).remove();
					if((message1 && message1 != '') || (message2 && message2 != '')){
						if(message1 == 'successfullySyncedMessage'){
							thisPlayer.createStats();
						}
						else{
							$('.sivaPlayer_statsPopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).append('<div class="sivaPlayer_message">' + ((message1 && message1 != '') ? thisPlayer.getLabel(message1) : message2) + thisPlayer.getLabel('tryAgain') + '</div>');
						}
					}
				});
			});
			controls.append(stats);
		}
		if(Modernizr.fullscreen){
			controls.append('<td class="sivaPlayer_spacer"></td>');
			var fullscreenButton = $('<td class="sivaPlayer_button"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_buttonNormalScreen" title="' + this.getLabel('closeFullscreenTooltip') + '"><polygon points="16.863,13.127 18.451,2.821 21.17,5.542 26.721,-0.008 29.972,3.245 24.422,8.794 27.171,11.541 "/><polygon points="13.138,16.873 11.55,27.178 8.831,24.457 3.28,30.008 0.027,26.756 5.577,21.206 2.831,18.459"/><polygon points="13.136,13.103 2.83,11.514 5.549,8.793 0,3.243 3.252,-0.008 8.802,5.542 11.549,2.794"/><polygon points="16.863,16.898 27.171,18.486 24.45,21.206 30,26.756 26.747,30.008 21.197,24.457 18.451,27.204"/></g><g class="sivaPlayer_buttonFullScreen" title="' + this.getLabel('fullscreenTooltip') + '"><polygon points="30,0.001 28.412,10.302 25.695,7.583 20.148,13.131 16.896,9.878 22.443,4.333 19.698,1.586"/><polygon points="0,29.999 1.588,19.699 4.306,22.418 9.852,16.871 13.104,20.121 7.557,25.668 10.302,28.415"/><polygon points="0,0 10.302,1.586 7.584,4.307 13.13,9.854 9.879,13.104 4.331,7.557 1.587,10.302"/><polygon points="30,30 19.698,28.414 22.417,25.693 16.871,20.147 20.121,16.898 25.669,22.444 28.414,19.699"/></g></svg></td>')
			.click(function(){
				thisPlayer.tidyPlayer();
				thisPlayer.changeFullscreenMode();
			});
			controls.append(fullscreenButton);
		}
		controls.append('<td class="sivaPlayer_spacer"></td>');
		$(this.player).append($('<table class="sivaPlayer_controls sivaPlayer_topControls sivaPlayer_transition"></table').append(controls));
		if(this.history.length <= 1){
			$('.sivaPlayer_prevButton', this.player).addClass('sivaPlayer_disabled');
		}
		this.setFullscreenButton();
	};
	
	this.setSubtitleButton = function(){
		if(this.isSubTitleVisible){
			$('.sivaPlayer_subtitleOnButton', this.player).show(0);
			$('.sivaPlayer_subtitleOffButton', this.player).hide(0);
		}
		else{
			$('.sivaPlayer_subtitleOnButton', this.player).hide(0);
			$('.sivaPlayer_subtitleOffButton', this.player).show(0);
		}
	};
	
	this.setSearchButton = function(){
		if($('.sivaPlayer_searchSidebar', this.player).length == 0){
			$('.sivaPlayer_searchOnButton', thisPlayer.player).show(0);
			$('.sivaPlayer_searchOffButton', thisPlayer.player).hide(0);
		}
		else{
			$('.sivaPlayer_searchOnButton', thisPlayer.player).hide(0);
			$('.sivaPlayer_searchOffButton', thisPlayer.player).show(0);
		}
	};
	
	this.setAnnotationButton = function(){
		if(this.isAnnotationVisible){
			$('.sivaPlayer_annotationOnButton', this.player).show(0);
			$('.sivaPlayer_annotationOffButton', this.player).hide(0);
		}
		else{
			$('.sivaPlayer_annotationOnButton', this.player).hide(0);
			$('.sivaPlayer_annotationOffButton', this.player).show(0);
		}
	};
	
	this.createVolumeControl = function(){
		var thisPlayer = this;
		$('.sivaPlayer_controls .sivaPlayer_volumeButton', this.player).append('<div class="sivaPlayer_volumeControl"></div>');
		for(var i = 100; i >= 0; i -= 25){
			$('.sivaPlayer_controls .sivaPlayer_volumeControl', this.player).append('<span class="sivaPlayer_volume ' + ((this.volume * 100 == i) ? 'sivaPlayer_current' : '') + '">' + i + '%</span>');
		}
		$('.sivaPlayer_controls .sivaPlayer_volumeControl .sivaPlayer_volume', this.player).hover(function(){
			$(this).addClass('sivaPlayer_hover');
		},
		function(){
			$(this).removeClass('sivaPlayer_hover');
		})
		.click(function(){
			var value = $(this).text().replace(/%/, '');
			thisPlayer.logAction('selectVolume', '', value + '%');
			thisPlayer.volume = value / 100;
			thisPlayer.setVolume(true, true);
		});
		this.setProportions();
	};
	
	this.changeFullscreenMode = function(){
		if(!document.fullScreen && !document.mozFullScreen && !document.webkitIsFullScreen){
			thisPlayer.logAction('openFullscreen', '', '');
			if(this.player.requestFullscreen){
				this.player.requestFullscreen();
			}
			else if(this.player.msRequestFullscreen){
				this.player.msRequestFullscreen();
			}
			else if(this.player.mozRequestFullScreen){
				this.player.mozRequestFullScreen();
			}
			else if(this.player.webkitRequestFullScreen){
				this.player.webkitRequestFullScreen();
			}
			$('.sivaPlayer_topControls .sivaPlayer_buttonFullScreen', this.player).css('display', 'none');
			$('.sivaPlayer_topControls .sivaPlayer_buttonNormalScreen', this.player).css('display', 'inline');
		}
		else{
			thisPlayer.logAction('closeFullscreen', '', '');
			if(document.exitFullscreen){
				document.exitFullscreen();
			}
			else if(document.msExitFullscreen){
				document.msExitFullscreen();
			}
			else if(document.mozCancelFullScreen){
				document.mozCancelFullScreen();
			}
			else if(document.webkitCancelFullScreen){
				document.webkitCancelFullScreen();
			}
			$('.sivaPlayer_topControls .sivaPlayer_buttonFullScreen', this.player).css('display', 'inline');
			$('.sivaPlayer_topControls .sivaPlayer_buttonNormalScreen', this.player).css('display', 'none');
		}
		this.setFullscreenButton();
	};
	
	this.setFullscreenButton = function(){
		if(!document.fullScreen && !document.mozFullScreen && !document.webkitIsFullScreen){
			$('.sivaPlayer_topControls .sivaPlayer_buttonFullScreen', this.player).css('display', 'inline');
			$('.sivaPlayer_topControls .sivaPlayer_buttonNormalScreen', this.player).css('display', 'none');
		}
		else{
			$('.sivaPlayer_topControls .sivaPlayer_buttonFullScreen', this.player).css('display', 'none');
			$('.sivaPlayer_topControls .sivaPlayer_buttonNormalScreen', this.player).css('display', 'inline');
		}
	};
	
	this.createStats = function(){
		var thisPlayer = this;
		this.startLoader();
		$.ajax({
			'async': true,
			'data': {'ajax': 'true'},
			'dataType': 'JSON',
			'crossDomain': true,
			'timeout': 60000,
			'type': 'GET',
			'url': ((this.configuration.common.logPath) ? this.configuration.common.logPath : this.configuration.videoPath) + '/getStats.js?token=' + ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) ? this.configuration.accessRestriction.accessToken : '&token2=' + this.logKey) + '&email=' + ((this.configuration.common.userEmail) ? this.configuration.common.userEmail : '') + '&secret=' + ((this.configuration.common.userSecret) ? this.configuration.common.userSecret : ''),
			'error': function(){
				$('.sivaPlayer_statsPopup .sivaPlayer_message', thisPlayer.player).remove();
				$('.sivaPlayer_statsPopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).append('<div class="sivaPlayer_message">' + thisPlayer.getLabel(data.description) + '</div>');
			},
			'success': function(data){
				if(!data.message){
					thisPlayer.drawStats(data);
				}
				else{
					$('.sivaPlayer_statsPopup .sivaPlayer_message', thisPlayer.player).remove();
					$('.sivaPlayer_statsPopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).append('<div class="sivaPlayer_message">' + thisPlayer.getLabel(data.description) + '</div>');
				}
			}
		});
	};
	
	this.drawStats = function(data){
		$('.sivaPlayer_statsPopup .sivaPlayer_content .sivaPlayer_scrollable', this.player).empty();
		var width = $('.sivaPlayer_statsPopup .sivaPlayer_content .sivaPlayer_scrollable', this.player).width();
		var cellSize = parseInt((width - 60) / 52);
		var height = cellSize * 7 + 10;
		var day = d3.time.format('%w');
		var week = d3.time.format('%U');
		var format = d3.time.format('%Y-%m-%d');
		var svg = d3.select(this.player)
		.select('.sivaPlayer_statsPopup .sivaPlayer_content .sivaPlayer_scrollable')
		.selectAll('svg')
		.data(d3.range(2014, ((new Date()).getFullYear()) + 1))
		.enter()
		.append('svg')
	    .attr('width', width)
	    .attr('height', height)
	    .append('g')
	    .attr('transform', 'translate(' + ((width - cellSize * 53) / 2) + ',' + (height - cellSize * 7 - 1) + ')');
		svg.append('text')
	    .attr('transform', 'translate(-6,' + cellSize * 3.5 + ')rotate(-90)')
	    .style('text-anchor', 'middle')
	    .text(function(d){
	    	return d;
	    });

		var rect = svg.selectAll('.day')
	    .data(function(d){
	    	return d3.time.days(new Date(d, 0, 1), new Date(d + 1, 0, 1));
	    })
	    .enter().append('rect')
	    .attr('class', 'day')
	    .attr('width', cellSize)
	    .attr('height', cellSize)
	    .attr('x', function(d){
	    	return week(d) * cellSize;
	    })
	    .attr('y', function(d){
	    	return day(d) * cellSize;
	    })
	    .datum(format);

		rect.append('title')
	    .text(function(d){
	    	return d;
	    });

		svg.selectAll('.month')
	    .data(function(d){
	    	return d3.time.months(new Date(d, 0, 1), new Date(d + 1, 0, 1));
	    })
	    .enter().append('path')
	    .attr('class', 'month')
	    .attr('d', function(t0){
			var t1 = new Date(t0.getFullYear(), t0.getMonth() + 1, 0),
			d0 = +day(t0), w0 = +week(t0),
			d1 = +day(t1), w1 = +week(t1);
			return 'M' + (w0 + 1) * cellSize + ',' + d0 * cellSize + 'H' + w0 * cellSize + 'V' + 7 * cellSize + 'H' + w1 * cellSize + 'V' + (d1 + 1) * cellSize + 'H' + (w1 + 1) * cellSize + 'V' + 0 + 'H' + (w0 + 1) * cellSize + 'Z';
		});

		rect.filter(function(d){
			return d in data;
		})
		.attr('style', function(d){
			var percent = data[d] / 40;
			if(percent > 1){
				percent = 1;
			}
			return 'fill: rgba(' + thisPlayer.hexToRgb(thisPlayer.configuration.style.secondaryColor) + ', ' + percent + ');';
		})
		.select('title')
		.text(function(d){
			return d + ' - ' + thisPlayer.formatTime(data[d] * 60);
		});		
		thisPlayer.stopLoader();
	};
	
	this.createAnnotationSidebar = function(){
		var thisPlayer = this;
		var sidebar = $('<div class="sivaPlayer_annotationSidebar"></div>')
		.css({'width': parseInt(this.configuration.style.annotationSidebarWidth * 100) + '%', 'right': '-' + parseInt(this.configuration.style.annotationSidebarWidth * 100) + '%'});
		$(sidebar).append(((this.configuration.common.collaboration) ? '<tr class="sivaPlayer_sidebarTabs"><td class="sivaPlayer_spacer"></td><td class="sivaPlayer_authorAnnotations">' + this.getLabel('authorAnnotationTab') + '</td><td class="sivaPlayer_communityAnnotations">' + this.getLabel('communityAnnotationTab') + '</td><td class="sivaPlayer_spacer"></td></tr>' : '') + '<div class="sivaPlayer_authorAnnotations"></div><div class="sivaPlayer_communityAnnotations"></div>');
		if(this.visibleSidebarAnnotationType == 'sivaPlayer_authorAnnotations'){
			$('.sivaPlayer_authorAnnotations', sidebar).addClass('sivaPlayer_active');
			$('.sivaPlayer_communityAnnotations', sidebar).removeClass('sivaPlayer_active');
		}
		else{
			$('.sivaPlayer_authorAnnotations', sidebar).removeClass('sivaPlayer_active');
			$('.sivaPlayer_communityAnnotations', sidebar).addClass('sivaPlayer_active');
		}
		$('.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations, .sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations', sidebar)
		.click(function(){
			thisPlayer.tidyPlayer();
			thisPlayer.logAction('changeAnnotationAreaTab', (($(this).hasClass('sivaPlayer_authorAnnotations')) ? 'author' : 'community'), '');
			$('.sivaPlayer_authorAnnotations, .sivaPlayer_communityAnnotations', sidebar).removeClass('sivaPlayer_active');
			thisPlayer.visibleSidebarAnnotationType = $(this).attr('class');
			$('.' + $(this).attr('class'), sidebar).addClass('sivaPlayer_active');
		})
		.hover(function(){
			$('.sivaPlayer_authorAnnotations, .sivaPlayer_communityAnnotations', sidebar).removeClass('sivaPlayer_active');
			$('.' + $(this).attr('class'), sidebar).addClass('sivaPlayer_active');
		}, function(){
			$('.sivaPlayer_authorAnnotations, .sivaPlayer_communityAnnotations', sidebar).removeClass('sivaPlayer_active');
			$('.' + thisPlayer.visibleSidebarAnnotationType, sidebar).addClass('sivaPlayer_active');
		});
		if(!this.configuration.common.annotationSidebarVisibility || (this.configuration.common.annotationSidebarVisibility != 'always' && this.configuration.common.annotationSidebarVisibility != 'never')){
			var annotationSidebarButton = $('<div class="sivaPlayer_annotationSidebarButton"><span class="sivaPlayer_button"><svg title="' + this.getLabel('openAnnotationSidebarTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_annotationSidebarOpenButton"><polygon points="27.536,0 12.5,15 27.536,30 28.9,28.5 15.4,15 28.9,1.5 "/><polygon points="16.537,0 1.5,15 16.537,30 17.9,28.5 4.4,15 17.9,1.5 "/></g><g class="sivaPlayer_annotationSidebarCloseButton"><polygon points="2.864,0 17.9,15 2.864,30 1.5,28.5 15,15 1.5,1.5 "/><polygon points="13.863,0 28.9,15 13.863,30 12.5,28.5 26,15 12.5,1.5 "/></g></svg></span></div>')
			.click(function(){
				thisPlayer.tidyPlayer();
				if(!thisPlayer.isAnnotationSidebarVisible){
					thisPlayer.logAction('openAnnotationArea', '', '');
					thisPlayer.slideInAnnotationSidebar();
				}
				else{
					thisPlayer.logAction('closeAnnotationArea', '', '');
					thisPlayer.slideOutAnnotationSidebar();
				}
			});
			$(this.player).append(annotationSidebarButton);
		}
		$(this.player).append(sidebar);
		$('.sivaPlayer_annotationSidebar, .sivaPlayer_annotationSidebarButton', this.player)
		.bind('swipeleft', function(e){
			if(!$('.sivaPlayer_annotationSidebar', this.player).hasClass('sivaPlayer_fullscreen')){
				thisPlayer.tidyPlayer();
				thisPlayer.slideInAnnotationSidebar();
				e.stopPropagation();
			}
		})
		.bind('swiperight', function(e){
			if(!$('.sivaPlayer_annotationSidebar', this.player).hasClass('sivaPlayer_fullscreen')){
				thisPlayer.tidyPlayer();
				thisPlayer.slideOutAnnotationSidebar();
				e.stopPropagation();
			}
		});
	};
	
	this.slideInAnnotationSidebar = function(){
		thisPlayer = this;
		if($('.sivaPlayer_annotationSidebar').hasClass('sivaPlayer_update')){
			return;
		}
		if(!this.configuration.common.annotationSidebarVisibility || this.configuration.common.annotationSidebarVisibility != 'never'){
			$('.sivaPlayer_volumeControl', this.player).remove();
			$('.sivaPlayer_annotationSidebar', this.player).addClass('sivaPlayer_update');
			$('.sivaPlayer_videoContainer', this.player).css(this.getVideoContainerProportions($('.sivaPlayer_mainVideo', this.player), $(this.player).width(), $(this.player).height(), true, true));
			$('.sivaPlayer_annotationSidebarButton', this.player).css({'right': parseInt(thisPlayer.configuration.style.annotationSidebarWidth * 100) + '%'});
			$('.sivaPlayer_annotationSidebar', this.player).css({'right': '0'});
			setTimeout(function(){
				thisPlayer.isAnnotationSidebarVisible = true;
				$('.sivaPlayer_annotationSidebarButton', thisPlayer.player).addClass('sivaPlayer_open');
				$('.sivaPlayer_annotationSidebar', this.player).removeClass('sivaPlayer_update');
			}, 800);			
		}
	};
	
	this.slideOutAnnotationSidebar = function(){
		thisPlayer = this;
		if($('.sivaPlayer_annotationSidebar').hasClass('sivaPlayer_update')){
			return;
		}
		if(!this.configuration.common.annotationSidebarVisibility || this.configuration.common.annotationSidebarVisibility != 'always'){
			this.annotationSidebarOpener = '';
			$('.sivaPlayer_volumeControl', this.player).remove();
			$.each($('.sivaPlayer_annotationSidebar video', this.player), function(){
				this.pause();
			});
			$('.sivaPlayer_annotationSidebar', this.player).addClass('sivaPlayer_update');
			$('.sivaPlayer_videoContainer', this.player).css(this.getVideoContainerProportions($('.sivaPlayer_mainVideo', this.player), $(this.player).width(), $(this.player).height(), false, true));
			$('.sivaPlayer_annotationSidebarButton', this.player).css({'right': 0});
			$('.sivaPlayer_annotationSidebar', this.player).css({'right': '-' + parseInt(thisPlayer.configuration.style.annotationSidebarWidth * 100) + '%'});
			setTimeout(function(){
				thisPlayer.isAnnotationSidebarVisible = false;
				$('.sivaPlayer_annotationSidebarButton', thisPlayer.player).removeClass('sivaPlayer_open');
				$('.sivaPlayer_annotationSidebar', this.player).removeClass('sivaPlayer_update');
			}, 800);
		}
	};
	
	this.createAnnotations = function(){
		var thisPlayer = this;
		for(var i = 0; i < this.configuration.globalAnnotations.length; i++){
			var annotation = this.configuration.annotations[(this.configuration.globalAnnotations[i])];
			this.createAnnotation(annotation, -1);
		}
		for(var i = 0; i < this.configuration.scenes[this.currentScene].annotations.length; i++){
			var trigger = this.configuration.scenes[this.currentScene].annotations[i];
			var annotation = this.configuration.annotations[trigger.annotationId];
			this.createAnnotation(annotation, trigger.triggerId);
			if(trigger.start > 1 && annotation.type != 'subTitle'){
				this.createTimelineMarker(trigger.start);
			}
		}
		this.createMediaAnnotationControls();
		$('.sivaPlayer_richtextImage', this.player).attr('src', function(index, src){
			$(this).attr('src', thisPlayer.configuration.videoPath + src);
		});
		$('.sivaPlayer_annotation.sivaPlayer_imageAnnotation img, .sivaPlayer_annotation.sivaPlayer_galleryAnnotation img', this.player).hover(function(){
			$(this).addClass('sivaPlayer_hover');
		}, function(){
			$(this).removeClass('sivaPlayer_hover');
		})
		.click(function(){
			thisPlayer.tidyPlayer();
			var annotationId = thisPlayer.getAnnotationId($(this).closest('.sivaPlayer_annotation').attr('class'));
			thisPlayer.logAction('openImageAnnotation', annotationId, $(this).attr('src').replace(new RegExp(thisPlayer.configuration.videoPath, 'g'), '').split('?')[0]);
			thisPlayer.createImageZoom(this);
		});
		$('.sivaPlayer_annotation.sivaPlayer_imageAnnotation .sivaPlayer_title, .sivaPlayer_annotation.sivaPlayer_galleryAnnotation .sivaPlayer_title', this.player).click(function(){
			thisPlayer.tidyPlayer();
			var annotationId = thisPlayer.getAnnotationId($(this).closest('.sivaPlayer_annotation').attr('class'));
			var image = $('img', $(this).closest('.sivaPlayer_annotation'))[0];
			thisPlayer.logAction('openImageAnnotation', annotationId, $(image).attr('src').replace(new RegExp(thisPlayer.configuration.videoPath, 'g'), '').split('?')[0]);
			thisPlayer.createImageZoom(image);
		});
		$('.sivaPlayer_pdfAnnotation', this.player).click(function(e){
			thisPlayer.tidyPlayer();
			var annotation = $(this).closest('.sivaPlayer_annotation');
			var annotationId = thisPlayer.getAnnotationId($(annotation).attr('class'));
			var link = $('a', annotation);
			thisPlayer.logAction('openPdfAnnotation', annotationId, $(link).attr('href'));
			thisPlayer.createPdfZoom(link);
			e.preventDefault();
		});
		$('.sivaPlayer_richtextAnnotation', this.player).click(function(e){
			thisPlayer.tidyPlayer();
			var annotationId = thisPlayer.getAnnotationId($(this).attr('class'));
			thisPlayer.logAction('openRichtextAnnotation', annotationId, $('.sivaPlayer_title', this).text());
			thisPlayer.createRichtextZoom(annotationId);
			e.preventDefault();
		});
		$('.sivaPlayer_annotation.sivaPlayer_videoAnnotation .sivaPlayer_title', this.player).click(function(){
			thisPlayer.tidyPlayer();
			var annotation = $(this).closest('.sivaPlayer_annotation');
			var annotationId = thisPlayer.getAnnotationId($(annotation).attr('class'));
			thisPlayer.logAction('manageMediaAnnotation', annotationId, 'openFullscreen');
			thisPlayer.openMediaAnnotationFullscreen(annotation, annotationId);
		});
		$('.sivaPlayer_markerButton span, .sivaPlayer_markerEllipse ellipse, .sivaPlayer_markerRectangle rect, .sivaPlayer_markerPolygon polygon', this.player).hover(function(e){
			$(this).parent().parent().addClass('sivaPlayer_active');
			e.stopPropagation();
		}, function(e){
			$(this).parent().parent().removeClass('sivaPlayer_active');
			e.stopPropagation();
		})
		.click(function(e){
			thisPlayer.tidyPlayer();
			var annotationId = thisPlayer.getAnnotationId($(this).parent().parent().attr('class'));
			var annotation = thisPlayer.configuration.annotations[annotationId];
			thisPlayer.logAction('clickMarkerAnnotation', annotationId, '');
			var targetAnnotation = thisPlayer.configuration.annotations[(annotation.target)];
			if($('.sivaPlayer_annotation.sivaPlayer_' +  targetAnnotation.id + '_' + targetAnnotation.triggerId + ':visible', thisPlayer.player).length == 0){
				var videoElements = $('.sivaPlayer_mainVideo', this.player);
				if(videoElements.length > 0){
					thisPlayer.configuration.scenes[thisPlayer.currentScene].annotations[targetAnnotation.triggerId].start = thisPlayer.currentSceneTime;
					thisPlayer.configuration.scenes[thisPlayer.currentScene].annotations[targetAnnotation.triggerId].end = thisPlayer.currentSceneTime + annotation.duration;
				}
			}
			else{
				thisPlayer.configuration.scenes[thisPlayer.currentScene].annotations[targetAnnotation.triggerId].start = -1;
				thisPlayer.configuration.scenes[thisPlayer.currentScene].annotations[targetAnnotation.triggerId].end = -1;
			}
			thisPlayer.updateAnnotations();
			e.stopPropagation();
		});
		$('.sivaPlayer_videoContainer .sivaPlayer_annotation', this.player).click(function(e){
			if(!$(this).hasClass('sivaPlayer_subTitle') && $('.sivaPlayer_markerEllipse, .sivaPlayer_markerPolygon', this).length == 0){
				e.stopPropagation();
			}
		})
		.mouseover(function(e){
			if(!$(this).hasClass('sivaPlayer_subTitle') && $('.sivaPlayer_markerEllipse, .sivaPlayer_markerPolygon', this).length == 0){
				e.stopPropagation();
			}
		});
		this.updateAnnotations();
	};
	
	this.createAnnotation = function(annotation, triggerId){
		var content = '';
		var css = '';
		if(annotation.type == 'image'){
			content = '<img src="' + ((annotation.preview) ? this.getThumbnailURL(this.configuration.videoPath + annotation.content[this.currentLanguage].href) : this.configuration.videoPath + annotation.content[this.currentLanguage].href + ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) ? '?token=' + this.configuration.accessRestriction.accessToken : '')) + '" alt="' + this.getLabel('imageAnnotationAlt') + '" title="' + this.getLabel('imageAnnotationTooltip') + '" />';
			css = 'sivaPlayer_imageAnnotation';
		}
		else if(annotation.type == 'richText'){
			content = this.shortenText(annotation.content[this.currentLanguage].content, 150);
			css = 'sivaPlayer_richtextAnnotation';
		}
		else if(annotation.type == 'pdf'){
			content = '<a href="' + this.configuration.videoPath + annotation.content[this.currentLanguage].href + '" target="_blank">' + this.getLabel('pdfAnnotation_link') + '</a>';
			css = 'sivaPlayer_pdfAnnotation';
		}
		else if(annotation.type == 'gallery'){
			content = '<table><tr>';
			var i = 0;
			for(; i < annotation.images.length; i++){
				if(i > 0 && i % annotation.columns == 0){
					content += '</tr><tr>';
				}
				content += '<td><img src="' + this.getThumbnailURL(this.configuration.videoPath + annotation.images[i][this.currentLanguage].href) + '" alt="' + this.getLabel('imageAnnotationAlt') + '" title="' + this.getLabel('imageAnnotationTooltip') + '" class="sivaPlayer_galleryImage" /></td>';
			}
			for(; i % annotation.columns != 0; i++){
				content += '<td class="sivaPlayer_empty"></td>';
			}
			content += '</tr></table>';
			css = 'sivaPlayer_galleryAnnotation';
		}
		else if(annotation.type == 'video'){
			content = '<video>';
			for(var i = 0; i < annotation.files.length; i++){
				content += '<source src="' + this.configuration.videoPath + annotation.files[i].url[this.currentLanguage].href + '.' + annotation.files[i].format + ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) ? '?token=' + this.configuration.accessRestriction.accessToken : '') + '" type="' + annotation.files[i].type + '" />';
			}
			content += '</video>';
			css = 'sivaPlayer_videoAnnotation';
		}
		else if(annotation.type == 'audio'){
			content = '<audio>';
			for(var i = 0; i < annotation.files.length; i++){
				content += '<source src="' + this.configuration.videoPath + annotation.files[i].url[this.currentLanguage].href + '.' + annotation.files[i].format + ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) ? '?token=' + this.configuration.accessRestriction.accessToken : '') + '" type="' + annotation.files[i].type + '" />';
			}
			content += '</audio>';
			css = 'sivaPlayer_audioAnnotation';
		}
		else if(annotation.type == 'subTitle'){
			content = '<table><tr><td>' + annotation.content[this.currentLanguage].content + '</td></tr></table>';
			css = 'sivaPlayer_subTitle';
		}
		else if(annotation.type == 'markerButton'){
			content = '<span class="sivaPlayer_markerButton" title="' + this.getLabel('markerTooltip') + '"><span>' + annotation.title[this.currentLanguage].content + '</span></span>';
		}
		else if(annotation.type == 'markerEllipse'){
			content = '<svg class="sivaPlayer_markerEllipse" title="' + this.getLabel('markerTooltip') + '" height="106px" width="106px"><ellipse cx="53" cy="53" rx="50" ry="50" /></svg>';
		}
		else if(annotation.type == 'markerRectangle'){
			content = '<svg class="sivaPlayer_markerRectangle" title="' + this.getLabel('markerTooltip') + '" height="106px" width="106px"><rect width="50" height="50" /></svg>';
		}
		else if(annotation.type == 'markerPolygon'){
			content = '<svg class="sivaPlayer_markerPolygon" title="' + this.getLabel('markerTooltip') + '" height="100%" width="100%"><polygon points="" /></svg>';
		}
		
		if(annotation.type == 'markerButton' || annotation.type == 'markerEllipse' || annotation.type == 'markerRectangle' || annotation.type == 'markerPolygon'){
			var tmpTriggerId = this.configuration.scenes[this.currentScene].annotations.length;
			this.configuration.scenes[this.currentScene].annotations.push({"start": -1.0, "end": -1.0, "annotationId": annotation.target, "triggerId": tmpTriggerId, "helper": true});
			this.configuration.annotations[annotation.target].triggerId = tmpTriggerId;
		}
		
		content = '<div class="sivaPlayer_annotation sivaPlayer_' + annotation.id + '_' + triggerId + ' ' + css + '">' + content + '</div>';
		
		if(annotation.title && annotation.title[this.currentLanguage] && annotation.title[this.currentLanguage].content != '' && !(annotation.type == 'subTitle' || annotation.type == 'markerButton' || annotation.type == 'markerEllipse' || annotation.type == 'markerRectangle')){
			content = $(content).prepend('<span class="sivaPlayer_title">' + annotation.title[this.currentLanguage].content + '</span>');
		}
		
		if(annotation.isSidebarAnnotation){
			$('.sivaPlayer_annotationSidebar .sivaPlayer_authorAnnotations', this.player).prepend(content);
		}
		else{
			$('.sivaPlayer_videoContainer', this.player).append(content);
		}
		
		if(triggerId < 0){
			$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + triggerId).fadeIn(500);
		}
	};
	
	this.updateAnnotations = function(){
		var thisPlayer = this;
		var videoElement = $('.sivaPlayer_mainVideo', this.player);
		if(videoElement.length == 0){
				return;
		}
		var mute = false;
		var unmute = false;
		var annotations = [];
		for(var i = 0; i < this.configuration.globalAnnotations.length; i++){
			var annotation = this.configuration.annotations[(this.configuration.globalAnnotations[i])];
			annotations.push({'annotation': annotation, 'trigger': {'triggerId': -1, 'start': 0}});
		}
		for(var i = 0; i < this.configuration.scenes[this.currentScene].annotations.length; i++){
			var trigger = this.configuration.scenes[this.currentScene].annotations[i];
			var annotation = this.configuration.annotations[trigger.annotationId];
			annotations.push({'annotation': annotation, 'trigger': trigger});
			if(annotation.type == 'markerButton' || annotation.type == 'markerEllipse' || annotation.type == 'markerRectangle' || annotation.type == 'markerPolygon'){
				annotation = this.configuration.annotations[annotation.target];
				annotations.push({'annotation': annotation, 'trigger': trigger});
			}
		}
		for(var i = 0; i < annotations.length; i++){
			var annotation = annotations[i].annotation;
			var trigger = annotations[i].trigger;
			if(!annotation.isSidebarAnnotation && annotation.path){
				var start = 0;
				var pathKey = -1;
				for(var j = 0; j < annotation.path.length; j++){
					if(annotation.path[j].start >= start && annotation.path[j].start <= this.currentSceneTime){
						start = annotation.path[j].start;
						pathKey = j;
					}
					else{
						break;
					}
				}
				if(pathKey > -1){
					var path = annotation.path[pathKey];
					if(annotation.type != 'markerPolygon'){
						var cssUpdate = {
								'top': (parseInt(path.top * 100 * 100) / 100) + '%',
								'left': (parseInt(path.left * 100 * 100) / 100) + '%'
						};
						if(path.width){
							cssUpdate.width = (parseInt(path.width * 100 * 100) / 100) + '%';
						}
						if(path.height){
							cssUpdate.height = (parseInt(path.height * 100 * 100) / 100) + '%';
						}
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId).css(cssUpdate);
					}
					var videoWidth = $('.sivaPlayer_videoContainer', this.player).width();
					var videoHeight = $('.sivaPlayer_videoContainer', this.player).height();
					if(annotation.type == 'markerEllipse'){
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg').attr('width', parseInt(path.width * videoWidth) + 'px');
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg').attr('height', parseInt(path.height * videoHeight) + 'px');
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg ellipse').attr('rx', parseInt(path.width * videoWidth / 2) - 3);
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg ellipse').attr('ry', parseInt(path.height * videoHeight / 2) - 3);
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg ellipse').attr('cx', parseInt(path.width * videoWidth / 2) + 3);
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg ellipse').attr('cy', parseInt(path.height * videoHeight / 2) + 3);
					}
					else if(annotation.type == 'markerRectangle'){
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg').attr('width', parseInt(path.width * videoWidth) + 'px');
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg').attr('height', parseInt(path.height * videoHeight) + 'px');
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg rect').attr('width', parseInt(path.width * videoWidth) - 6);
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg rect').attr('height', parseInt(path.height * videoHeight) - 6);
					}
					else if(annotation.type == 'markerPolygon'){
						var minX = undefined;
						var minY = undefined;
						var maxX = undefined;
						var maxY = undefined;
						for(var j = 0; j < path.vertices.length; j++){
							var y = path.vertices[j].top;
							var x = path.vertices[j].left;
							if(!minY || y < minY){
								minY = y;
							}
							if(!minX || x < minX){
								minX = x;
							}
							if(!maxY || y > maxY){
								maxY = y;
							}
							if(!maxX || x > maxX){
								maxX = x;
							}
						}
						var points = [];
						for(var j = 0; j < path.vertices.length; j++){
							var x = parseInt((path.vertices[j].left - minX) * videoWidth);
							var y = parseInt((path.vertices[j].top - minY) * videoHeight);
							points.push(x + ',' + y);
						}
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId).css({'top': parseInt(minY * videoHeight) + 'px', 'left': parseInt(minX * videoWidth) + 'px', 'width': parseInt((maxX - minX) * videoWidth) + 'px', 'height': parseInt((maxY - minY) * videoHeight) + 'px'});
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg').attr('width', parseInt((maxX - minX) * videoWidth) + 'px');
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg').attr('height', parseInt((maxY - minY) * videoHeight) + 'px');
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' svg polygon').attr('points', points.join(' '));
					}
				}
			}
			if(((annotation.type != 'subTitle' && (annotation.isSidebarAnnotation || !annotation.disableable || this.isAnnotationVisible)) || (annotation.type == 'subTitle' && this.isSubTitleVisible)) && trigger.start <= this.currentSceneTime && (!trigger.end || trigger.end >= this.currentSceneTime)){
				if((!annotation.isSidebarAnnotation || $('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId, this.player).parent().hasClass('sivaPlayer_active')) && $('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ':hidden', this.player).length > 0){
					$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ':hidden', this.player).fadeIn({'duration': 500, 'done': function(a, t){
						if(a.isSidebarAnnotation){
							$('.sivaPlayer_annotationSidebar > .sivaPlayer_active', thisPlayer.player).scrollTop(0);
						}
						if(!a.isSidebarAnnotation || thisPlayer.isAnnotationSidebarVisible){
							if(a.pauseVideo){
								thisPlayer.pauseVideo();
							}
							if(a.muteVideo){
								mute = true;
							}
							if(a.type == 'video' && a.autostart){
								$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' video', thisPlayer.player)[0].play();
							}
							else if(a.type == 'audio' && a.autostart){
								$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' audio', thisPlayer.player)[0].play();
							}
						}
					}(annotation, trigger)});
				}
			}
			else{
				if($('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ':visible', this.player).length > 0){
					$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ':visible', this.player).fadeOut(500, function(a, t){
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId, thisPlayer.player).removeClass('sivaPlayer_active');
						if(a.isSidebarAnnotation){
							$('.sivaPlayer_annotationSidebar > .sivaPlayer_active', thisPlayer.player).scrollTop(0);
						}
						if(!a.isSidebarAnnotation || thisPlayer.isAnnotationSidebarVisible){
							if(a.muteVideo){
								unmute = true;
							}
						}
						if(a.type == 'video'){
							$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' video', thisPlayer.player)[0].pause();
						}
						else if(a.type == 'audio'){
							$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ' audio', thisPlayer.player)[0].pause();
						}
						if(t.helper){
							thisPlayer.configuration.scenes[thisPlayer.currentScene].annotations[t.triggerId].start = -1;
							thisPlayer.configuration.scenes[thisPlayer.currentScene].annotations[t.triggerId].end = -1;
						}
					}(annotation, trigger));
				}
			}
		}
		thisPlayer.setProportions();
		if(mute && this.previousVolume == -1){
			this.previousVolume = this.volume;
			this.volume = 0;
			this.setVolume(false, true);
		}
		else if(unmute && this.previousVolume > -1){
			this.volume = this.previousVolume;
			this.setVolume(true, true);
		}
	};
	
	this.createMediaAnnotationControls = function(){
		var thisPlayer = this;
		$.each($('.sivaPlayer_annotation video, .sivaPlayer_annotation audio', this.player), function(){
			var duration = this.duration;
			if(!duration || isNaN(duration)){
				duration = 0;
			}
			$(this).after('<span class="sivaPlayer_mediaAnnotationControlsHolder"><table class="sivaPlayer_mediaAnnotationControls"><tr><td class="sivaPlayer_button sivaPlayer_playPauseButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="20px" height="20px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_playButton" title="' + thisPlayer.getLabel('playTooltip') + '"><polygon points="2,0 2,30 28,15"/></g><g class="sivaPlayer_pauseButton" title="' + thisPlayer.getLabel('pauseTooltip') + '"><rect x="2" width="10.4" height="30"/><rect x="17.6" width="10.4" height="30"/></g><g class="sivaPlayer_replayButton" title="' + thisPlayer.getLabel('replayTooltip') + '"><path d="M14.928,1.838c-2.375,0-4.605,0.599-6.566,1.64L4.192,0L2.209,14.822l12.707-5.878l-3.802-3.17 c1.188-0.448,2.469-0.706,3.813-0.706c5.992,0,10.852,4.857,10.852,10.852c0,5.994-4.859,10.851-10.852,10.851 c-5.12,0-9.401-3.55-10.542-8.32l-3.395-0.622C1.924,24.699,7.799,30,14.928,30c7.777,0,14.082-6.304,14.082-14.081 C29.01,8.142,22.705,1.838,14.928,1.838z"/></g></svg></td><td class="sivaPlayer_timelineCurrentTime">00:00</td><td class="sivaPlayer_timeline"><span class="sivaPlayer_timelineProgress"><span class="sivaPlayer_timelineProgressBar"><span></span></span></span></td><td class="sivaPlayer_timelineDuration">' + thisPlayer.formatTime(duration) + '</td><td class="sivaPlayer_button sivaPlayer_volumeButton"><svg title="' + thisPlayer.getLabel('volumeTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="20px" height="20px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_muteButton"><path d="M26.533,2.611L0,26.595l1.427,1.712L27.961,4.322L26.533,2.611z M18.289,15.655c-0.045,6.851-0.784,12.307-1.695,12.307c-0.75,0-1.777-3.695-2.161-8.811l-4.978,4.494 c2.116,3.11,4.46,6.354,5.05,6.354h2.472c1.634,0,2.861-6.75,2.861-15.033l-0.006-0.705L18.289,15.655z M4.294,20.623h0.195 l10.383-9.378c-0.139-0.134-0.289-0.25-0.445-0.345c0.384-5.15,1.412-8.861,2.167-8.861c0.623,0,1.162,2.539,1.462,6.328 l1.389-1.256C18.972,2.839,18.105,0,17.044,0h-2.539c-0.8,0-4.395,5.64-6.694,9.378H4.294c-0.895,0-1.623,2.495-1.623,5.623 C2.672,18.128,3.4,20.623,4.294,20.623z"/></g><g class="sivaPlayer_unmuteButton"><path d="M26.881,4.771L25.27,5.718c1.268,2.919,1.939,6.145,1.939,9.357 s-0.672,6.438-1.939,9.357l1.611,0.947c1.377-3.221,2.1-6.778,2.1-10.305C28.98,11.543,28.258,7.99,26.881,4.771z M24.525,9.226 l-1.717,0.839c0.635,1.587,1.127,3.309,1.127,5.03s-0.492,3.469-1.127,5.056l1.639,0.569c0.762-1.875,1.268-3.692,1.268-5.708 C25.715,13.033,25.273,11.088,24.525,9.226z M20.527,13.424c0.494,1.07,0.486,2.454-0.021,3.796l1.496,0.565 c0.658-1.741,0.65-3.574-0.02-5.03L20.527,13.424z M16.999,0H14.45c-0.799,0-4.408,5.638-6.713,9.375H6.108h-1.51h-0.39 c-0.894,0-1.623,2.497-1.623,5.625c0,3.129,0.729,5.625,1.623,5.625h1.899v0.021h1.268C9.694,24.16,13.664,30,14.45,30h2.479 c1.639,0,2.871-6.75,2.871-15.032S18.633,0,16.999,0z M16.547,27.96c-0.756,0-1.786-3.716-2.173-8.858 c1.052-0.644,1.797-2.235,1.797-4.102c0-1.871-0.745-3.458-1.797-4.102c0.387-5.147,1.417-8.857,2.173-8.857 c0.943,0,1.705,5.801,1.705,12.959C18.252,22.152,17.49,27.96,16.547,27.96z"/></g></svg></td>' + (($(this).prop('tagName').toLowerCase() == 'video') ? '<td class="sivaPlayer_button sivaPlayer_fullScreenButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="20px" height="20px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_buttonNormalScreen" title="' + thisPlayer.getLabel('closeFullscreenTooltip') + '"><polygon points="16.863,13.127 18.451,2.821 21.17,5.542 26.721,-0.008 29.972,3.245 24.422,8.794 27.171,11.541 "/><polygon points="13.138,16.873 11.55,27.178 8.831,24.457 3.28,30.008 0.027,26.756 5.577,21.206 2.831,18.459"/><polygon points="13.136,13.103 2.83,11.514 5.549,8.793 0,3.243 3.252,-0.008 8.802,5.542 11.549,2.794"/><polygon points="16.863,16.898 27.171,18.486 24.45,21.206 30,26.756 26.747,30.008 21.197,24.457 18.451,27.204"/></g><g class="sivaPlayer_buttonFullScreen" title="' + thisPlayer.getLabel('fullscreenTooltip') + '"><polygon points="30,0.001 28.412,10.302 25.695,7.583 20.148,13.131 16.896,9.878 22.443,4.333 19.698,1.586"/><polygon points="0,29.999 1.588,19.699 4.306,22.418 9.852,16.871 13.104,20.121 7.557,25.668 10.302,28.415"/><polygon points="0,0 10.302,1.586 7.584,4.307 13.13,9.854 9.879,13.104 4.331,7.557 1.587,10.302"/><polygon points="30,30 19.698,28.414 22.417,25.693 16.871,20.147 20.121,16.898 25.669,22.444 28.414,19.699"/></g></svg></td>' : '') + '</tr></table></span>');
			$(this).after('<span class="sivaPlayer_mediaHolder"></span>');
			var mediaHolder = $(this).siblings('.sivaPlayer_mediaHolder');
			$(this).prependTo(mediaHolder);
			if($(this).prop('tagName').toLowerCase() == 'video'){
				$(mediaHolder).append('<span class="sivaPlayer_overlayButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="80px" height="80px" viewBox="0 0 300 300"><circle cx="150" cy="150" r="150"/><g class="sivaPlayer_playButton" title="' + thisPlayer.getLabel('playTooltip') + '"><polygon points="108.5,82.767 108.5,225.856 232.5,154.31 "/></g><g class="sivaPlayer_replayButton" title="' + thisPlayer.getLabel('replayTooltip') + '"><path d="M149.539,64.941c-15.144,0-29.366,3.874-41.87,10.596L81.087,53.065l-12.647,95.786l81.024-37.991 l-24.24-20.486c7.577-2.896,15.741-4.557,24.315-4.557c38.209,0,69.193,31.392,69.193,70.128c0,38.73-30.984,70.12-69.193,70.12 c-32.647,0-59.939-22.94-67.226-53.769l-21.645-4.021c5.953,44.404,43.414,78.658,88.87,78.658 c49.592,0,89.792-40.736,89.792-90.989C239.331,105.678,199.131,64.941,149.539,64.941z"/></g></svg></span>');
				$(mediaHolder).click(function(){
					var annotationId = thisPlayer.getAnnotationId($(this).parent().attr('class'));
					var mediaElement = $('video', this)[0];
					thisPlayer.logAction('manageMediaAnnotation', annotationId, ((mediaElement.paused) ? ((!mediaElement.ended) ? 'play' : 'replay') : 'pause'));
					if(mediaElement.paused){
						mediaElement.play();
					}
					else{
						mediaElement.pause();
					}				
				})
				.hover(function(){
					$(this).addClass('sivaPlayer_hover');
				}, function(){
					$(this).removeClass('sivaPlayer_hover');
				});	
			}
			$(this).unbind()
			.bind('loadeddata', function(){
				$('.sivaPlayer_timelineDuration', $(this).parent().parent()).text(thisPlayer.formatTime(this.duration));				
			})
			.bind('play', function(){
				if(this.ended){
					this.currentTime = 0;
					this.ended = false;
				}
				var annotationId = thisPlayer.getAnnotationId($(this).parent().parent().attr('class'));
				if(thisPlayer.configuration.annotations[annotationId] && !(thisPlayer.configuration.annotations[annotationId].playSynced || thisPlayer.configuration.annotations[annotationId].playFree)){
					$('.sivaPlayer_videoContainer .sivaPlayer_mainVideo', thisPlayer.player)[0].pause();
				}
				$('.sivaPlayer_playButton, .sivaPlayer_replayButton, .sivaPlayer_overlayButton', $(this).parent().parent()).hide(0);
				$('.sivaPlayer_pauseButton', $(this).parent().parent()).show(0);
				thisPlayer.playingMediaAnnotations.push(1);
				if(thisPlayer.playingMediaAnnotations.length == 1){
					thisPlayer.updateMediaAnnotationTimelines(false);
				}
			})
			.bind('pause', function(){
				if(!this.ended){
					$('.sivaPlayer_playButton, .sivaPlayer_overlayButton', $(this).parent().parent()).show(0);
					$('.sivaPlayer_pauseButton', $(this).parent().parent()).hide(0);
				}
				thisPlayer.playingMediaAnnotations.pop();
			})
			.bind('volumechange', function(){
				var annotationId = thisPlayer.getAnnotationId($(this).parent().parent().attr('class'));
				if(this.volume == 0){
					thisPlayer.logAction('manageMediaAnnotation', annotationId, 'mute');
					$('.sivaPlayer_muteButton', $(this).parent().parent()).show(0);
					$('.sivaPlayer_unmuteButton', $(this).parent().parent()).hide(0);
				}
				else{
					thisPlayer.logAction('manageMediaAnnotation', annotationId, 'unmute');
					$('.sivaPlayer_muteButton', $(this).parent().parent()).hide(0);
					$('.sivaPlayer_unmuteButton', $(this).parent().parent()).show(0);
				}
			})
			.bind('ended', function(){
				this.ended = true;
				this.pause();
				var annotationId = thisPlayer.getAnnotationId($(this).parent().parent().attr('class'));
				thisPlayer.logAction('manageMediaAnnotation', annotationId, 'end');
				$('.sivaPlayer_replayButton, .sivaPlayer_overlayButton', $(this).parent().parent()).show(0);
				$('.sivaPlayer_playButton, .sivaPlayer_pauseButton', $(this).parent().parent()).hide(0);
			});		
			var controls = $('.sivaPlayer_mediaAnnotationControls', $(this).parent().parent());
			$('.sivaPlayer_playPauseButton', controls).click(function(){
				var mediaElement = $(this).closest('.sivaPlayer_annotation').find('video, audio')[0];
				var annotationId = thisPlayer.getAnnotationId($(this).closest('.sivaPlayer_annotation').attr('class'));
				thisPlayer.logAction('manageMediaAnnotation', annotationId, ((mediaElement.paused) ? ((!mediaElement.ended) ? 'play' : 'replay') : 'pause'));
				if(mediaElement.paused){
					mediaElement.play();
				}
				else{
					mediaElement.pause();
				}				
			});
			$('.sivaPlayer_timelineProgress', controls)
			.bind('mousemove', function(e){
				var mediaElement = $(this).closest('.sivaPlayer_annotation').find('video, audio')[0];
				if($('.sivaPlayer_timelineUpdate', controls).length == 0){
					$(this).append('<span class="sivaPlayer_timelineUpdate"><span></span></span><span class="sivaPlayer_timlineUpdateSelectedTime"></span>');
				}
				var offset = $(this).offset();
				var width = (e.pageX - offset.left);
				if(width + 1 > $(this).width()){
					width = $(this).width() - 1;
				}
				$('.sivaPlayer_timelineUpdate span', controls).css('width', width + 'px');
				var percentage = (e.pageX - $(this).offset().left) / $(this).width();
				var duration = mediaElement.duration;
				var timeSelectionWidth = $('.sivaPlayer_timlineUpdateSelectedTime', controls).width();
				var timeSelectionLeft = parseInt(e.pageX - $(this).offset().left - timeSelectionWidth / 2);
				if(timeSelectionLeft < 0){
					timeSelectionLeft = 0;
				}	
				else if(timeSelectionLeft + timeSelectionWidth > $(this).width()){
					timeSelectionLeft = $(this).width() - timeSelectionWidth;
				}
				$('.sivaPlayer_timlineUpdateSelectedTime', controls).text(thisPlayer.formatTime(duration * percentage))
					.css('left', timeSelectionLeft + 'px');
			})
			.bind('mouseleave', function(){
				$('.sivaPlayer_timelineUpdate, .sivaPlayer_timlineUpdateSelectedTime', this).remove();
			})
			.click(function(e){
				var mediaElement = $(this).closest('.sivaPlayer_annotation').find('video, audio')[0];
				var percentage = (e.pageX - $(this).offset().left) / $(this).width();
				var duration = mediaElement.duration;
				var annotationId = thisPlayer.getAnnotationId($(this).closest('.sivaPlayer_annotation').attr('class'));
				thisPlayer.logAction('manageMediaAnnotation', annotationId, 'seek');
				mediaElement.currentTime = duration * percentage;
				if(mediaElement.paused){
					$('.sivaPlayer_playButton, .sivaPlayer_overlayButton', $(this).closest('.sivaPlayer_annotation')).show(0);
					$('.sivaPlayer_replayButton, .sivaPlayer_pauseButton', $(this).closest('.sivaPlayer_annotation')).hide(0);
				}
				thisPlayer.updateMediaAnnotationTimelines(true);
			});
			$('.sivaPlayer_volumeButton', controls).click(function(){
				var mediaElement = $(this).closest('.sivaPlayer_annotation').find('video, audio')[0];
				mediaElement.volume = ((mediaElement.volume == 0) ? 1 : 0);	
			});
			$('.sivaPlayer_fullScreenButton', controls).click(function(){
				var videoElement = $(this).closest('.sivaPlayer_annotation')[0];
				var annotationId = thisPlayer.getAnnotationId($(videoElement).attr('class'));
				if(!$(videoElement).hasClass('sivaPlayer_fullscreen')){
					thisPlayer.logAction('manageMediaAnnotation', annotationId, 'openFullscreen');
					thisPlayer.openMediaAnnotationFullscreen(videoElement);
				}
				else{
					thisPlayer.logAction('manageMediaAnnotation', annotationId, 'closeFullscreen');
					thisPlayer.closeMediaAnnotationFullscreen(videoElement);
				}
						
			});
			this.volume = thisPlayer.volume;
		});
	};
	
	this.openMediaAnnotationFullscreen = function(videoElement){
		thisPlayer = this;
		$(videoElement).append('<span class="sivaPlayer_closeButton sivaPlayer_button"><svg title="' + thisPlayer.getLabel('closeTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" width="52.455px" height="52.455px" viewBox="0 0 52.455 52.455" xml:space="preserve"><path d="M-0.163,1l2.114-1.003L52.618,50.33l-2.114,2.128L-0.163,1z"/><path d="M50.333,0l2.122,2.122L2.122,52.455L0,50.333L50.333,0z"/></svg></span>')
		.addClass('sivaPlayer_transition')
		.addClass('sivaPlayer_fullscreen')
		.css('display', 'table');
		$('.sivaPlayer_mediaAnnotationControls .sivaPlayer_button svg', videoElement).attr('width', '30px').attr('height', '30px');
		$('.sivaPlayer_overlayButton svg', videoElement).attr('width', '300px').attr('height', '300px');
		$(videoElement).closest('.sivaPlayer_videoContainer, .sivaPlayer_annotationSidebar').addClass('sivaPlayer_fullscreen');
		if($('.sivaPlayer_videoContainer', thisPlayer.player).hasClass('sivaPlayer_fullscreen')){
			$('.sivaPlayer_videoBackground', thisPlayer.player).addClass('sivaPlayer_transition')
			.addClass('sivaPlayer_fullscreen');
		}
		$('.sivaPlayer_closeButton', videoElement).click(function(){
			thisPlayer.logAction('manageMediaAnnotation', thisPlayer.getAnnotationId($(videoElement).attr('class')), 'closeFullscreen');
			thisPlayer.closeMediaAnnotationFullscreen(videoElement);
		});
		$('.sivaPlayer_buttonNormalScreen', videoElement).show();
		$('.sivaPlayer_buttonFullScreen', videoElement).hide();
		thisPlayer.setPlayedBefore($('video', videoElement)[0]);
		setTimeout(function(){
			thisPlayer.setProportions();
		}, 500);
	};
	
	this.closeMediaAnnotationFullscreen = function(videoElement){
		thisPlayer = this;
		$(videoElement).removeClass('sivaPlayer_fullscreen')
		.css('display', 'block');
		$('video', videoElement).css('height', 'auto');
		$('.sivaPlayer_videoBackground, .sivaPlayer_videoContainer, .sivaPlayer_annotationSidebar', this.player).removeClass('sivaPlayer_fullscreen');
		$('.sivaPlayer_mediaAnnotationControls .sivaPlayer_button svg', videoElement).attr('width', '20px').attr('height', '20px');
		$('.sivaPlayer_overlayButton svg', videoElement).attr('width', '80px').attr('height', '80px');
		$('.sivaPlayer_mediaTop, .sivaPlayer_closeButton', videoElement).remove();
		$('.sivaPlayer_buttonNormalScreen', videoElement).hide(0);
		$('.sivaPlayer_buttonFullScreen', videoElement).show(0);
		if($('video', videoElement)[0].paused){
			this.restorePlayedBefore();
		}
		this.clearPlayedBefore();
		setTimeout(function(){
			thisPlayer.setProportions();
		}, 500);
	};
	
	this.updateMediaAnnotationTimelines = function(alwaysUpdate){
		var thisPlayer = this;
		if(thisPlayer.playingMediaAnnotations.length == 0 && !alwaysUpdate){
			return;
		}
		$.each($('.sivaPlayer_annotation video, .sivaPlayer_annotation audio', this.player), function(){
			var currentTime = this.currentTime;
			var duration = this.duration;
			if(!duration || isNaN(duration)){
				return;
			}
			var percentage = (parseInt((currentTime * 100 / duration) * 100) / 100);
			if(percentage > 100){
				percentage = 100;
			}
			$('.sivaPlayer_timelineProgress .sivaPlayer_timelineProgressBar span', $(this).parent().parent()).css('width', percentage + '%');
			$('.sivaPlayer_timelineCurrentTime', $(this).parent().parent()).text(thisPlayer.formatTime(currentTime));
		});
		if(!alwaysUpdate){
			setTimeout(function(){
				thisPlayer.updateMediaAnnotationTimelines(false);
			}, 500);
		}
	};
	
	this.createPdfZoom = function(pdfElement){
		var thisPlayer = this;
		thisPlayer.setPlayedBefore();
		thisPlayer.createPopup('pdf', true);
		$('.sivaPlayer_pdfPopup .sivaPlayer_closeButton', this.player).click(function(){
			thisPlayer.logAction('closePdfAnnotation', '', '');
		});
		$('.sivaPlayer_pdfPopup .sivaPlayer_title', this.player).text($('.sivaPlayer_title', $(pdfElement).closest('.sivaPlayer_annotation')).text());		
		$('.sivaPlayer_pdfPopup .sivaPlayer_content', this.player).empty().append(this.getLabel('pdfAnnotation_error') + '<iframe src="' + $(pdfElement).attr('href') + '"></iframe>');
	};
	
	this.createRichtextZoom = function(annotationId){
		var thisPlayer = this;
		thisPlayer.setPlayedBefore();
		thisPlayer.createPopup('richtext', true);
		$('.sivaPlayer_richtextPopup .sivaPlayer_closeButton', this.player).click(function(){
			thisPlayer.logAction('closeRichtextAnnotation', '', '');
		});
		var annotation = this.configuration.annotations[annotationId];
		console.log(this.configuration.annotations, annotationId);
		$('.sivaPlayer_richtextPopup .sivaPlayer_title', this.player).text(annotation.title[this.currentLanguage].content);		
		$('.sivaPlayer_richtextPopup .sivaPlayer_scrollable', this.player).empty().append(annotation.content[this.currentLanguage].content);
	};
	
	this.createImageZoom = function(imageElement){
		var thisPlayer = this;
		thisPlayer.setPlayedBefore();
		thisPlayer.createPopup('zoom', true);
		$('.sivaPlayer_zoomPopup .sivaPlayer_closeButton div', this.player).click(function(){
			thisPlayer.logAction('closeImageAnnotation', '', '');
		});
		$('.sivaPlayer_zoomPopup .sivaPlayer_title', this.player).text($('.sivaPlayer_title', $(imageElement).closest('.sivaPlayer_annotation')).text());		
		$('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).empty().append('<img src="' + this.getOriginalImageURL($(imageElement).attr('src')) + '" alt="' + this.getLabel('zoomImageTooltip') + '" />');
		if($(imageElement).hasClass('sivaPlayer_galleryImage')){
			var thumbnails = '';
			$.each($('img', $(imageElement).closest('table')), function(){
				thumbnails += '<img src="' + $(this).attr('src') + '" ' + ((this == imageElement) ? 'class="sivaPlayer_current"' : '') + ' />';
			});
			$('.sivaPlayer_zoomPopup tr:last-child', this.player).after('<tr class="sivaPlayer_galleryThumbnails"><td colspan="3"><div>' + thumbnails + '</div></td></tr>');
			$('.sivaPlayer_galleryThumbnails img', this.player).hover(function(){
				$(this).addClass('sivaPlayer_hover');
			}, function(){
				$(this).removeClass('sivaPlayer_hover');
			})
			.click(function(){
				thisPlayer.logAction('changeOpenedImage', 'imageClick', $(this).attr('src'));
				thisPlayer.setZoomImage(this);
			});
			
			var prevButton = $('<svg title="' + this.getLabel('previousImageTooltip') + '" class="sivaPlayer_galleryPreviousButton sivaPlayer_button" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="300px" height="300px" viewBox="0 0 300 300" xml:space="preserve"><circle cx="150.483" cy="150" r="150"/><polygon points="205.209,26.998 84.854,147.053 205.209,267.107 216.116,255.103 108.067,147.053 216.116,39.003 "/></svg>')
			.click(function(){
				thisPlayer.setPreviousZoomImage();
			});
			
			$('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).prepend(prevButton);
			var nextButton = $('<svg title="' + this.getLabel('nextImageTooltip') + '" version="1.1" class="sivaPlayer_galleryNextButton sivaPlayer_button" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="300px" height="300px" viewBox="0 0 300 300" xml:space="preserve"><circle cx="150.482" cy="150" r="150"/><polygon points="95.759,267.109 216.114,147.055 95.759,27 84.852,39.005 192.9,147.055 84.852,255.104 "/></svg>')
			.click(function(){
				thisPlayer.setNextZoomImage();
			});
			$(document).keydown(function(e){
				if(e.keyCode == '37'){
					thisPlayer.setPreviousZoomImage();
				}
				else if(e.keyCode == '39'){
					thisPlayer.setNextZoomImage();
				}
			});
			$('.sivaPlayer_zoomPopup .sivaPlayer_closeButton', this.player).click(function(){
				$(document).unbind('keydown')
				.keydown(function(e) {
				    if(e.keyCode == 32) {
				        return false;
				    }
				});
			});
			
			$('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).append(nextButton);
			thisPlayer.setProportions();
		}
		else{
			$('.sivaPlayer_zoomPopup tr:last-child', this.player).after('<tr class="sivaPlayer_spacer"><td colspan="3"></td></tr>');
		}
	};
	
	this.setNextZoomImage = function(){
		var images = $('.sivaPlayer_galleryThumbnails img', thisPlayer.player);
		var i = 0;
		for( ; i < images.length; i++){
			if($(images[i]).hasClass('sivaPlayer_current')){
				break;
			}
		}
		i = (i + 1) % images.length;
		thisPlayer.logAction('changeOpenedImage', 'nextButton', $(images[i]).attr('src'));
		thisPlayer.setZoomImage(images[i]);
	};
	
	this.setPreviousZoomImage = function(){
		var images = $('.sivaPlayer_galleryThumbnails img', thisPlayer.player);
		var i = 0;
		for( ; i < images.length; i++){
			if($(images[i]).hasClass('sivaPlayer_current')){
				break;
			}
		}
		i--;
		if(i < 0){
			i = images.length - 1;
		}
		thisPlayer.logAction('changeOpenedImage', 'prevButton', $(images[i]).attr('src'));
		thisPlayer.setZoomImage(images[i]);
	};
	
	this.setZoomImage = function(image){
		$('.sivaPlayer_galleryThumbnails img', thisPlayer.player).removeClass('sivaPlayer_current');
		$(image).addClass('sivaPlayer_current');
		$('.sivaPlayer_zoomPopup .sivaPlayer_content img', thisPlayer.player).attr('src', this.getOriginalImageURL($(image).attr('src')));
		thisPlayer.setProportions();
	};
	
	this.getThumbnailURL = function(src){
		src = src.split('?token=')[0].split('.');
		var extension = src.pop();
		return src.join('.') + '_thumb.' + extension + ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) ? '?token=' + this.configuration.accessRestriction.accessToken : ''); 
	};
	
	this.getOriginalImageURL = function(src){
		if(src.indexOf('_thumb.') == -1)
			return src;
		src = src.split('?token=')[0].split('_thumb.');
		var extension = src.pop();
		return src.join('_thumb.') + '.' + extension + ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) ? '?token=' + this.configuration.accessRestriction.accessToken : '');
	};
	
	this.formatTime = function(time){
		time = parseInt(time);
		var hours = (time - (time % 3600)) / 3600;
		time -= hours * 3600;
		var minutes = (time - (time % 60)) / 60;
		time -= minutes * 60;
		var seconds = time;
		return ((hours > 0) ? this.addLeadingZero(hours) + ':' : '') + this.addLeadingZero(minutes) + ':' + this.addLeadingZero(seconds);
	};
	
	this.addLeadingZero = function(number){
		number = "" + number;
		if(number.length < 2){
			number = '0' + number;
		}
		return number;
	};
	
	this.timeToFloat = function(time){
		time = time.split('.');
		var milliSeconds = parseInt(time[1]) / 1000;
		time = time[0].split(':');
		var seconds = 0;
		for(var i = 0; i < time.length; i++){
			seconds += parseInt(time[i]) * ((i == 0) ? 3600 : ((i == 1) ? 60 : 1));
		}
		return seconds + milliSeconds; 
	};
	
	this.hexToRgb = function(hex){
		var r = parseInt(hex.substr(1, 2), 16);
		var g = parseInt(hex.substr(3, 2), 16);
		var b = parseInt(hex.substr(5, 4), 16);
		return r + ',' + g + ',' + b;
	};
	
	this.shortenText = function(text, maxLength){
		var newText = [];
		var newLength = 0;
		text = $('<div></div>').append(text).text().split(' ');
		var i = 0;
		for(; i < text.length && newLength <= maxLength; i++){
			var tmp = text[i];
			if(tmp.length > 0){
				newText.push(tmp);
				newLength += tmp.length + 1;
			}
		}
		newText = newText.join(' ');
		if(i < text.length - 1){
			newText += '...';
		}
		return newText + ' <span class="sivaPlayer_readMoreLink">' + this.getLabel('readMoreLink') + '</span>';
	};
	
	this.getAnnotationId = function(classes){
		console.log(classes);
		var replace = ['annotation', 'imageAnnotation', 'galleryAnnotation', 'videoAnnotation', 'audioAnnotation', 'richtextAnnotation', 'pdfAnnotation', 'active', 'transition', 'fullscreen', ''];
		for(var i = 0; i < replace.length; i++){
			classes = classes.replace(new RegExp('sivaPlayer_' + replace[i], ''), '');
			console.log(classes, 'sivaPlayer_' + replace[i]);
		}
		classes = classes.trim().split('_');
		classes.pop();
		return classes.join('_');
	};
	
	this.fadeInFadeOutControls = function(delay){
		var thisPlayer = this;
		$('.sivaPlayer_mainVideo, .sivaPlayer_videoBackground', this.player).css('cursor', 'pointer');
		if($('.sivaPlayer_bottomControls', this.player).length > 0){
			$.when($('.sivaPlayer_controls', this.player).addClass('sivaPlayer_active')).done(function(){
				var previousDisplayTime = thisPlayer.controlsDisplayTime;
				thisPlayer.controlsDisplayTime = delay;
				if(previousDisplayTime <= 0){
					thisPlayer.fadeOutControls();
				}
			});	
		}
	};
	
	this.fadeOutControls = function(){
		thisPlayer = this;
		if(this.preventControlsFadeOut || this.controlsDisplayTime <= 0){
			this.controlsDisplayTime = 0;
			return;
		}
		this.controlsDisplayTime--;
		if(this.controlsDisplayTime > 0 && $('.sivaPlayer_controls:visible', this.player).length > 0){
			setTimeout(function(){
				thisPlayer.fadeOutControls();
			}, 1000);
		}
		else{
			this.controlsDisplayTime = 0;
			$('.sivaPlayer_controls', this.player).removeClass('sivaPlayer_active');
			$('.sivaPlayer_volumeUpdate', this).remove();
			var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player);
			if(videoElement.length > 0 && !videoElement[0].paused){
				$('.sivaPlayer_mainVideo, .sivaPlayer_videoBackground', this.player).css('cursor', 'none');
			}
		}
	};
	
	this.setCurrentSceneTime = function(time, noVideoUpdate){
		var videoElements = $('.sivaPlayer_mainVideo', this.player);
		if(videoElements.length > 0){
			if(!this.currentSceneEnded && time > videoElements[0].duration){
				this.onSceneEnd(videoElements[0]);
			}
			else{
				this.currentSceneTime = time;
				if(!noVideoUpdate){
					videoElements[0].currentTime = time;
				}
			}
			if(this.history.length <= 1 && time <= 5){
				$('.sivaPlayer_prevButton', this.player).addClass('sivaPlayer_disabled');
			}
			else{
				$('.sivaPlayer_prevButton', this.player).removeClass('sivaPlayer_disabled');
			}
		}
	};
	
	this.updateTimeline = function(alwaysUpdate){
		var thisPlayer = this;
		var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player);
		if((videoElement.length > 0 && !videoElement[0].paused) || alwaysUpdate){
			if(!alwaysUpdate){
				this.setCurrentSceneTime(videoElement[0].currentTime, true);
			}
			var percentage = (parseInt((this.currentSceneTime * 100 / videoElement[0].duration) * 100) / 100);
			if(percentage > 100){
				percentage = 100;
			}
			$('.sivaPlayer_controls .sivaPlayer_timelineProgress .sivaPlayer_timelineProgressBar span', this.player).css('width', percentage + '%');
			$('.sivaPlayer_controls .sivaPlayer_timelineCurrentTime', this.player).text(this.formatTime(this.currentSceneTime));
			$('.sivaPlayer_nodeSelectionSidebar', this.player).fadeOut(800, function(){
				$(this).remove();
			});
			this.updateAnnotations();
			if(!videoElement[0].paused){
				setTimeout(function(){
					thisPlayer.updateTimeline(false);
				}, 300);
			}
		}
	};
	
	this.createTimelineMarker = function(time){
		var videoElement = $('.sivaPlayer_mainVideo', this.player)[0];
		var percentage = time * 100 / videoElement.duration;
		if(percentage < 101){
			var marker = $('<span class="sivaPlayer_timelineMarker"></span>')
			.css('left', percentage + '%');
			$('.sivaPlayer_controls .sivaPlayer_timelineProgress', this.player).append(marker);
		}
	};
	
	this.createNodeSelectionPopup = function(node){
		var thisPlayer = this;
		this.pauseVideo();
		$('.sivaPlayer_nodeSelectionPopup, .sivaPlayer_nodeSelectionSidebar', this.player).remove();
		var isVideoElementAvailable = ($('.sivaPlayer_mainVideo', this.player).length > 0);
		var timeoutData = {};
		var nodes = '<div class="sivaPlayer_sceneList">';
		var sceneNode = {};
		if(node != this.configuration.endScene && node != this.configuration.endScene.node){
			sceneNode = this.configuration.sceneNodes[node];
			if(!sceneNode){
				this.throwError(new Error(this.getLabel('unknownSceneOrNode')), false);
			}
			for(var i = 0; i < sceneNode.next.length; i++){
				var n = sceneNode.next[i];
				var nNode = n.node;
				var disabled = false;
				if(n.condition && !this.checkConditions(n.condition.conditions)){
					if(!n.condition.visible){
						continue;					
					}
					else{
						disabled = true;
					}
				}
				if(nNode.split('-')[0] == 'random'){
					nNode = this.getRandomSelection(nNode);
				}
				if(n.timeout){
					timeoutData.nextNode = nNode;
					timeoutData.timeout = n.timeout;
				}	
				nodes += '<a href="' + this.generateNodeLink(nNode) + '" class="sivaPlayer_log_' + nNode + ((disabled) ? ' sivaPlayer_disabled' : '') + '" title="' + ((disabled) ? ((n.condition.message) ? n.condition.message[this.currentLanguage].content : this.getLabel('nodeSelectionDisabledTooltip')) : this.getLabel('nodeSelectionTooltip')) + '">' + ((n.image) ? '<img src="' + this.getThumbnailURL(this.configuration.videoPath + n.image[this.currentLanguage].href) + '" />' : '') + ((n.title) ? n.title[this.currentLanguage].content : '') + '</a><span></span>';
			}
		}
		nodes += '</div>';
		var title = ((sceneNode.title) ? ((sceneNode.title[this.currentLanguage].content) ? sceneNode.title[this.currentLanguage].content : '') : ((this.configuration.endScene.title) ? this.configuration.endScene.title[this.currentLanguage].content :this.getLabel('endNodeTitle')));
		this.logAction('openFork', node, title);
		if(isVideoElementAvailable){
			$('.sivaPlayer_topControls .sivaPlayer_prevButton, .sivaPlayer_topControls .sivaPlayer_nextButton', this.player).addClass('sivaPlayer_disabled');
			$('.sivaPlayer_topControls .sivaPlayer_sceneTitle span', this.player).fadeOut(500);
			$(this.player).append('<div class="sivaPlayer_nodeSelectionSidebar"><div class="sivaPlayer_holder"><div class="sivaPlayer_title">' + title + '</div></div></div>');
			$('.sivaPlayer_nodeSelectionSidebar .sivaPlayer_holder', this.player).append(nodes);
			$('.sivaPlayer_nodeSelectionSidebar', this.player).css('width', parseInt(this.configuration.style.nodeSelectionSidebarWidth * 100) + '%')
			.show(0);
		}
		else{
			this.createTopControls(node, false);
			this.createPopup('nodeSelection', false);
			$('.sivaPlayer_nodeSelectionPopup .sivaPlayer_title', this.player).text(title);
			$('.sivaPlayer_nodeSelectionPopup .sivaPlayer_content .sivaPlayer_scrollable', this.player).append(nodes);
			$('.sivaPlayer_nodeSelectionPopup a.sivaPlayer_current').click(function(){
				thisPlayer.closePopups(600);
			});
		}
		$('.sivaPlayer_nodeSelectionSidebar a, .sivaPlayer_nodeSelectionPopup a').click(function(e){
			if($(this).hasClass('sivaPlayer_disabled')){
				$('.sivaPlayer_nodeSelectionSidebar .sivaPlayer_holder, .sivaPlayer_nodeSelectionPopup .sivaPlayer_scrollable').scrollTop();
				$('.sivaPlayer_nodeSelectionSidebar .sivaPlayer_message, .sivaPlayer_nodeSelectionPopup .sivaPlayer_message').remove();
				$('.sivaPlayer_nodeSelectionPopup .sivaPlayer_scrollable').prepend('<span class="sivaPlayer_message">' + $(this).attr('title') + '</span>');
				$('.sivaPlayer_nodeSelectionSidebar .sivaPlayer_title').after('<span class="sivaPlayer_message">' + $(this).attr('title') + '</span>');
				$('.sivaPlayer_nodeSelectionSidebar .sivaPlayer_message, .sivaPlayer_nodeSelectionPopup .sivaPlayer_message').delay(3000).fadeOut(500, function(){
					$(this).remove();
				});
				e.preventDefault();
				e.stopPropagation();
			}
			else{
				thisPlayer.logAction('selectForkEntry', $(this).attr('class').replace(/sivaPlayer_current/, '').trim().replace(/sivaPlayer_log_/, ''), $(this).text());
			}
		});
		if(timeoutData.timeout){
			this.timeoutSelection(node, timeoutData.nextNode, timeoutData.timeout);			
		}
	};
	
	this.checkConditions = function(conditions, operation){
		var result = undefined;
		if($.isArray(conditions)){
			if(!operation){
				operation = 'AND';
			}
			result = (operation == 'AND') ? true : false;
			for(var i = 0; i < conditions.length; i++){
				var tmp;
				if($.isPlainObject(conditions[i])){
					tmp = this.checkConditions(conditions[i]);
				}
				else{
					tmp = this.sceneHistory[(conditions[i])] !== undefined;
				}
				if(operation == 'AND'){
					result = result && tmp;
				}
				else if(operation == 'OR'){
					result = result || tmp;
				}
				else if(operation == 'NOT'){
					result = !tmp;
				}
			}
			return result;
		}
		else{
			for(operation in conditions){
				result = this.checkConditions(conditions[operation], operation);
				return result;
			}
		}
	};
	
	this.getRandomSelection = function(node){
		var random = Math.random();
		var sum = 0;
		var sceneNode = this.configuration.sceneNodes[node];
		var n = {};
		for(var i = 0; i < sceneNode.next.length; i++){
			n = sceneNode.next[i];
			sum += n.probability;
			if(sum >= random){
				return n.node;
			}
		}
		return n.node;
	};
	
	this.timeoutSelection = function(currentNode, nextNode, timeLeft){
		var thisPlayer = this;
		var fragments = $.param.fragment().split('&');
		var currentFragment = '';
		for(var i = 0; i < fragments.length; i++){
			var f = fragments[i].split('=');
			if(f[0] == this.arrayPosition){
				currentFragment = this.removeTimestampFromFragment(f[1]);
				break;
			}
		}
		if(currentFragment == currentNode && $('.sivaPlayer_nodeSelectionPopup, .sivaPlayer_nodeSelectionSidebar').length > 0){
			$('.sivaPlayer_nodeSelectionPopup .sivaPlayer_title .sivaPlayer_timeout, .sivaPlayer_nodeSelectionSidebar .sivaPlayer_title .sivaPlayer_timeout').remove();
			$('.sivaPlayer_nodeSelectionPopup .sivaPlayer_title, .sivaPlayer_nodeSelectionSidebar .sivaPlayer_title').append('<span class="sivaPlayer_timeout"> (' + timeLeft + 's)</span>');
			if(timeLeft <= 0){
				var next = {};
				next[this.arrayPosition] = nextNode;
				$.bbq.pushState(next);
			}
			else{
				setTimeout(function(){
					if($('.sivaPlayer_popup:not(.sivaPlayer_nodeSelectionPopup)').length > 0){
						timeLeft++;
					}
					thisPlayer.timeoutSelection(currentNode, nextNode, timeLeft - 1);
				}, 1000);
			}
		}
	};
	
	this.createSearchSidebar = function(node){
		var thisPlayer = this;
		$(this.player).append('<div class="sivaPlayer_searchSidebar"><div class="sivaPlayer_holder"><div class="sivaPlayer_title">' + this.getLabel('searchTitle') + '</div></div></div>');
		$('.sivaPlayer_searchSidebar .sivaPlayer_holder', this.player).append('<div class="sivaPlayer_input"><input type="text" placeholder="' + this.getLabel('searchInputText') + '" /></div><div class="sivaPlayer_results"></div>');
		$('.sivaPlayer_searchSidebar', this.player).css('width', parseInt(this.configuration.style.annotationSidebarWidth * $(this.player).width() + 40) + 'px')
		.fadeIn(800);
		$('.sivaPlayer_searchSidebar input', this.player).focus().bind('input', function(){
			thisPlayer.logAction('searchFor', '', $(this).val());
			thisPlayer.searchFor($(this).val());
		});
	};
	
	this.searchFor = function(keywords){
		var thisPlayer = this;
		$('.sivaPlayer_searchSidebar .sivaPlayer_results', this.player).empty();
		keywords = keywords.replace(/\s+/, ' ').split(' ');
		var found = {};
		var inputSufficient = false;
		for(var i = 0; i < keywords.length; i++){
			if(keywords[i].length >= 2){
				inputSufficient = true;
				var foundInThisRound = [];
				for(var keyword in this.configuration.index){
					if(keyword.toLowerCase().indexOf(keywords[i].toLowerCase()) > -1){
						for(var j = 0; j < this.configuration.index[keyword].length; j++){
							var hit = this.configuration.index[keyword][j];
							var scene = this.configuration.scenes[hit.id];
							if(hit.annotation){
								var start = undefined;
								for(var k = 0; k < scene.annotations.length; k++){
									if(scene.annotations[k].annotationId == hit.annotation){
										start = scene.annotations[k].start;
										break;
									}
								}
								if(i == 0 && !found[hit.annotation]){
									found[hit.annotation] = '<a href="' + this.generateNodeLink(hit.id, start) + '" class="' + ((this.configuration.annotations[hit.annotation].isSidebarAnnotation) ? 'sivaPlayer_sidebarAnnotation' : '') + ' sivaPlayer_log_' + hit.id + '_' + hit.annotation + '" title="' + this.getLabel('searchAnnotationTooltip') + '">' + ((this.configuration.annotations[hit.annotation].title) ? this.configuration.annotations[hit.annotation].title[this.currentLanguage].content : this.getLabel(this.configuration.annotations[hit.annotation].type  + 'Annotation')) + ' <span>' + ((this.configuration.annotations[hit.annotation].title) ? this.getLabel(this.configuration.annotations[hit.annotation].type  + 'Annotation') + ' ' : '') + this.getLabel('searchAnnotationInSceneText') + ' <i>' + scene.title[this.currentLanguage].content + '</i>, ' + this.getLabel('searchAnnotationAtTimeText') + ' <i>' + this.formatTime(start) + '</i></span></a>';
								}
								foundInThisRound.push(hit.annotation);
							}
							else{
								if(i == 0 && !found[hit.id]){
									found[hit.id] = '<a href="' + this.generateNodeLink(hit.id) + '" class="sivaPlayer_log_' + hit.id + '" title="' + this.getLabel('searchSceneTootlip') + '">' + scene.title[this.currentLanguage].content + ' <span>' + this.getLabel('searchSceneText') + '</span></a>';
								}
								foundInThisRound.push(hit.id);
							}
						}
					}
				}
				for(var scene in this.configuration.scenes){
					if(this.configuration.scenes[scene].title[this.currentLanguage].content.toLowerCase().indexOf(keywords[i].toLowerCase()) > -1){
						if(i == 0 && !found[scene]){
							found[scene] = '<a href="' + this.generateNodeLink(scene) + '" class="sivaPlayer_log_' + scene + '" title="' + this.getLabel('searchSceneTootlip') + '">' + this.configuration.scenes[scene].title[this.currentLanguage].content + ' <span>' + this.getLabel('searchSceneText') + '</span></a>';
						}
						foundInThisRound.push(scene);
					}
				}
				for(var element in found){
					if(foundInThisRound.indexOf(element) == -1){
						delete found[element];
					}
				}
			}
		}
		var i = 0;
		for(var element in found){
			$('.sivaPlayer_searchSidebar .sivaPlayer_results', this.player).append(found[element]);
			i++;
		}
		if(i > 0){
			$('.sivaPlayer_searchSidebar a', this.player).click(function(){
				thisPlayer.logAction('selectSearchResult', $(this).attr('class').replace(/sivaPlayer_current/, '').trim().replace(/sivaPlayer_log_/, ''), $(this).text());
			});
			$('.sivaPlayer_searchSidebar .sivaPlayer_sidebarAnnotation', this.player).click(function(){
				thisPlayer.isAnnotationSidebarVisible = true;
			});
		}
		else if(inputSufficient){
			$('.sivaPlayer_searchSidebar .sivaPlayer_results', this.player).append('<span class="sivaPlayer_empty">' + this.getLabel('searchNoMatches') + '</span>');
		}
	};
	
	this.createSettingsPopup = function(){
		var thisPlayer = this;
		this.logAction('openSettings', '', '');
		this.createPopup('settings', true);
		$('.sivaPlayer_settingsPopup .sivaPlayer_closeButton div', this.player).click(function(){
			thisPlayer.logAction('closeSettings', '', '');
		});
		$('.sivaPlayer_settingsPopup .sivaPlayer_title', this.player).append(this.getLabel('settingsTitle'));
		var videoElement = $('.sivaPlayer_mainVideo', this.player);
		var offset = 0;
		if(videoElement.length > 0){
			offset = this.currentSceneTime;
		}
		var content = '<div class="sivaPlayer_title2">' + this.getLabel('languageSettingsTitle') + '</div><div class="sivaPlayer_sceneList">';
		for(var i = 0; i < this.configuration.languages.length; i++){
			var language = this.configuration.languages[i];
			content += '<a href="' + this.generateNodeLink(this.currentScene, offset) + '" class="' + ((language == this.currentLanguage) ? 'sivaPlayer_current' : '') + ' sivaPlayer_log_' + language + '" title="' + ((language == this.currentLanguage) ? this.getLabel('settingsCurrentLanguageLink') : this.getLabel('settingsChooseLanguageLink')) + '">' + language + '</a><span></span>';
		}
		if(this.configuration.common && this.configuration.common.log && (this.configuration.accessRestriction || this.configuration.common.logPath)){
			content += '<div class="sivaPlayer_title2 sivaPlayer_syncTitle">' + this.getLabel('syncSettingsTitle') + '</div><div><label><button>' + this.getLabel('syncButton') + ' (0 ' + this.getLabel('syncButtonSessions') + ')</button></label></div>';
		}
		$('.sivaPlayer_settingsPopup .sivaPlayer_content .sivaPlayer_scrollable', this.player).append(content + '</div>');
		if(this.configuration.common && this.configuration.common.log && (this.configuration.accessRestriction || this.configuration.common.logPath)){
				this.setSessionAmount();
		}
		$('.sivaPlayer_settingsPopup .sivaPlayer_sceneList a').click(function(e){
			if($(this).hasClass('sivaPlayer_current')){
				thisPlayer.closePopups(600);
				e.preventDefault();
			}
			else{
				var language = $(this).attr('class').replace(/sivaPlayer_current/, '').trim().replace(/sivaPlayer_log_/, '');
				thisPlayer.currentLanguage = language;
				thisPlayer.logAction('selectLanguage', language, $(this).text());
			}
		});		
		if(this.configuration.common && this.configuration.common.log && (this.configuration.accessRestriction || this.configuration.common.logPath)){
			$('.sivaPlayer_settingsPopup button').click(function(e){
				thisPlayer.logAction('syncManually', '', '');
				thisPlayer.startLoader();
				$('.sivaPlayer_loader .sivaPlayer_logo', thisPlayer.player).show(0);
				$('.sivaPlayer_loader .sivaPlayer_text', thisPlayer.player).text(thisPlayer.getLabel('syncing'));
				sivaPlayerClearLog(function(message1, message2){
					thisPlayer.stopLoader();
					$('.sivaPlayer_settingsPopup .sivaPlayer_message', thisPlayer.player).remove();
					if((message1 && message1 != '') || (message2 && message2 != '')){
						thisPlayer.setSessionAmount();
						$('.sivaPlayer_settingsPopup .sivaPlayer_syncTitle', thisPlayer.player).after('<div class="sivaPlayer_message">' + ((message1 && message1 != '') ? thisPlayer.getLabel(message1) : message2) + '</div>');						
					}
				});
			});
		}
	};
	
	this.setSessionAmount = function(){
		var thisPlayer = this;
		sivaPlayerStorage.getAll(function(result){
			var i = 0;
			for(var key in result){
				if(key.indexOf('sivaPlayerLog_') == 0){
					i++;
				}
			}
			$('.sivaPlayer_settingsPopup button', thisPlayer.player).text(thisPlayer.getLabel('syncButton') + ' (' + i + ' ' + ((i != 1) ? thisPlayer.getLabel('syncButtonSessions') : thisPlayer.getLabel('syncButtonSession')) + ')');
		});	
	};
	
	this.createTableOfContentsPopup = function(node){
		var thisPlayer = this;
		this.createPopup('tableOfContents', true);
		$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_closeButton div', this.player).click(function(){
			thisPlayer.logAction('closeTableOfContents', '', '');
		});
		var content;
		if(this.configuration.tableOfContents){
			content = '<div class="sivaPlayer_tableOfContents">';
			var firstLevelEntries = [];
			for(var e in this.configuration.tableOfContents.entries){
				if(this.configuration.tableOfContents.entries[e].isFirstLevelEntry){
					firstLevelEntries.push(e);
				}
			}
			content += this.generateTableOfContentsSubList(node, firstLevelEntries, 1);
			$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_title', this.player).text(this.configuration.tableOfContents.title[this.currentLanguage].content);			
		}
		else{
			content = '<div class="sivaPlayer_sceneList">';
			for(var sceneId in this.configuration.scenes){
				var scene = this.configuration.scenes[sceneId];
				content += '<a href="' + this.generateNodeLink(((sceneId == node) ? undefined : sceneId)) + '" class="' + ((sceneId == node) ? 'sivaPlayer_current' : '') + ' sivaPlayer_log_' + sceneId + '">' + scene.title[this.currentLanguage].content + '</a><span></span>';
			}
			$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_title', this.player).text(this.getLabel('sceneListTitle'));
		}
		if(this.configuration.common.copyright && this.configuration.common.copyright != ''){
			content += '<a class="sivaPlayer_level1 sivaPlayer_copyright">' + this.getLabel('copyright') + ' <svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="10px" height="10px" viewBox="0 0 30 30" xml:space="preserve"><g><polygon points="2,0 2,30 28,15"/></g></svg></a>';
		}
		if(this.configuration.common.imprint && this.configuration.common.imprint[this.currentLanguage].href != ''){
			content += '<a class="sivaPlayer_level1" href="' + this.configuration.common.imprint[this.currentLanguage].href + '" target="_blank">' + this.getLabel('imprint') + ' <svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="10px" height="10px" viewBox="0 0 30 30" xml:space="preserve"><g><polygon points="2,0 2,30 28,15"/></g></svg></a>';
		}
		if(this.configuration.common.privacy && this.configuration.common.privacy[this.currentLanguage].href != ''){
			content += '<a class="sivaPlayer_level1" href="' + this.configuration.common.privacy[this.currentLanguage].href + '" target="_blank">' + this.getLabel('privacy') + ' <svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="10px" height="10px" viewBox="0 0 30 30" xml:space="preserve"><g><polygon points="2,0 2,30 28,15"/></g></svg></a>';
		}
		content += '</div>';
		$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_content .sivaPlayer_scrollable', this.player).append(content);
		$('.sivaPlayer_tableOfContentsPopup a', this.player).click(function(e){
			if($(this).hasClass('sivaPlayer_disabled')){
				$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_scrollable').scrollTop();
				$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_message').remove();
				$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_scrollable').prepend('<span class="sivaPlayer_message">' + $(this).attr('title') + '</span>');
				$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_message').delay(3000).fadeOut(500, function(){
					$(this).remove();
				});
				e.preventDefault();
				e.stopPropagation();
			}
			else{
				thisPlayer.logAction('selectTableOfContentsEntry', $(this).attr('class').replace(/sivaPlayer_current/, '').trim().replace(/sivaPlayer_log_/, ''), $(this).text());
			}
		});
		if(this.configuration.common.copyright && this.configuration.common.copyright != ''){
			$('.sivaPlayer_copyright', this.player).click(function(){
				$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_title', this.player).text(thisPlayer.getLabel('copyright'));
				$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_content .sivaPlayer_scrollable', thisPlayer.player).html(thisPlayer.configuration.common.copyright);
			});
		}
		$('.sivaPlayer_tableOfContentsPopup a.sivaPlayer_current', this.player).click(function(){
			thisPlayer.closePopups(600);
		});		
	};
	
	this.generateTableOfContentsSubList = function(node, entries, level){
		var text = '';
		for(var i = 0; i < entries.length; i++){
			var entry = this.configuration.tableOfContents.entries[(entries[i])];
			if(entry.target){
				var visible = true;
				var condition = false;
				var enabled = false;
				var message = undefined;
				if(this.configuration.parentNodes){
					for(var j = 0; this.configuration.parentNodes[entry.target] && j < this.configuration.parentNodes[entry.target].length && !enabled; j++){
						var n = this.configuration.sceneNodes[(this.configuration.parentNodes[entry.target][j])];
						for(var k = 0; k < n.next.length && !enabled; k++){
							if(n.next[k].node == entry.target){
								if(n.next[k].condition){
									message = n.next[k].condition.message;
									if(!n.next[k].condition.visible){
										visible = false;
									}
									condition = true;
									enabled = this.checkParentsConditionsFulfilled(entry.target);
								}
								break;
							}
						}
					}
				}
				if(visible || enabled || !condition){
					text += '<a href="' + this.generateNodeLink(((entry.target == node) ? undefined : entry.target)) + '" class="' + ((entry.target == node) ? 'sivaPlayer_current' : '') + ' ' + ((level <= 3) ? 'sivaPlayer_level' + level : '') + ' sivaPlayer_log_' + entries[i] + ' ' + ((!enabled && condition) ? 'sivaPlayer_disabled' : '') + '" title="' + ((!enabled && condition) ? ((message) ? message[this.currentLanguage].content : this.getLabel('nodeSelectionDisabledTooltip')) : '') + '">' + entry.title[this.currentLanguage].content + ' <svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="10px" height="10px" viewBox="0 0 30 30" xml:space="preserve">' + ((entry.target != node) ? '<g><polygon points="2,0 2,30 28,15"/></g>' : '<g><rect x="2" width="10.4" height="30"/><rect x="17.6" width="10.4" height="30"/></g>') + '</svg></a>';
				}
			}
			else
				text += '<span class="' + ((level <= 2) ? 'sivaPlayer_level' + level : '') + '">' + entry.title[this.currentLanguage].content + '</span>';
			if(entry.subEntries.length > 0){
				text += '<span class="sivaPlayer_indent">' + this.generateTableOfContentsSubList(node, entry.subEntries, level + 1) + '</span>';
			}
		}
		return text;
	};
	
	this.checkParentsConditionsFulfilled = function(node){
		var fulfilled = true;
		if(this.configuration.parentNodes[node]){
			for(var i = 0; this.configuration.parentNodes[node] && i < this.configuration.parentNodes[node].length && fulfilled; i++){
				var n = this.configuration.sceneNodes[(this.configuration.parentNodes[node][i])];
				if(!n){
					fulfilled = this.checkParentsConditionsFulfilled(this.configuration.parentNodes[(this.configuration.parentNodes[node][i])]);
					continue;
				}
				for(var k = 0; k < n.next.length; k++){
					if(n.next[k].node == node){
						if(n.next[k].condition && !this.checkConditions(n.next[k].condition.conditions)){
							return false;
						}
					}
				}
				fulfilled = this.checkParentsConditionsFulfilled(this.configuration.parentNodes[node]);
			}
		}
		return fulfilled;
	};
	
	this.tidyPlayer = function(delay){
		if(!delay)
			delay = 800;
		this.restorePlayedBefore();
		$('.sivaPlayer_searchSidebar, .sivaPlayer_volumeControl, .sivaPlayer_popup:not(.sivaPlayer_nodeSelectionPopup)', this.player).fadeOut(delay, function(){
			$(this).remove();
			thisPlayer.setSearchButton();
		});		
	};
	
	this.createPopup = function(id, hasCloseButton){
		var thisPlayer = this;
		var popup = $('<div class="sivaPlayer_popup sivaPlayer_' + id + 'Popup' + ((hasCloseButton) ? ' sivaPlayer_closePopup' : '') + '"><table><tr><td class="sivaPlayer_title"></td></tr><tr><td class="sivaPlayer_content"><div class="sivaPlayer_scrollable"></div></td></tr></table><span class="sivaPlayer_button sivaPlayer_closeButton"><svg title="' + this.getLabel('closeTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" width="52.455px" height="52.455px" viewBox="0 0 52.455 52.455" xml:space="preserve"><path d="M-0.163,1l2.114-1.003L52.618,50.33l-2.114,2.128L-0.163,1z"/><path d="M50.333,0l2.122,2.122L2.122,52.455L0,50.333L50.333,0z"/></svg></span></div>');
		$('.sivaPlayer_closeButton.sivaPlayer_button svg', popup)
			.click(function(){
				thisPlayer.closePopups(600);
			});
		if(!hasCloseButton){
			$('.sivaPlayer_closeButton.sivaPlayer_button', popup).remove();
		}
		$(this.player).prepend(popup);
		this.setProportions();
	};
	
	this.closePopups = function(delay){
		this.tidyPlayer(delay);
	};
	
	this.generateNodeLink = function(nodeId, time){
		var currentFragments = '#' + $.param.fragment();
		if(!nodeId){
			return currentFragments;
		}
		var fragmentUpdate = {};
		var url;
		if(time){
			nodeId += ',' + time;
		}
		nodeId += '|' + this.hashchangeCounter;
		fragmentUpdate[this.arrayPosition] = nodeId;
		url = $.param.fragment(currentFragments, fragmentUpdate);
		return url;
	};
	
	this.removeTimestampFromFragment = function(fragment){
		return fragment.split(',')[0].replace(/%7C/gi, '|').split('|')[0];
	};
	
	this.checkIncludedLanguages = function(){
		for(var i = 0; i < this.configuration.languages.length; i++){
			var lang = this.configuration.languages[i].split('-')[0].toLowerCase();
			if(typeof sivaPlayerLanguage == 'undefined' || !sivaPlayerLanguage[lang]){
				this.throwError(new Error('Language pack <i>' + lang.toUpperCase() + '</i> is missing.'), false);
				break;
			}
		}
	};
	
	this.getLabel = function(key){
		if(!this.currentLanguage){
			for(var lang in sivaPlayerLanguage){
				this.currentLanguage = lang;
				break;
			}
			if(!this.currentLanguage){
				this.throwError(new Error('No language file defined.'), false);
			}
		}
		var language = this.currentLanguage.split('-')[0].toLowerCase();
		if(!sivaPlayerLanguage[language] || !sivaPlayerLanguage[language][key]){
			return key;
		}
		else{
			return sivaPlayerLanguage[language][key];
		}
	};
	
	this.logAction = function(type, element, extraInfo){
		if(this.configuration.common && this.configuration.common.log && (this.configuration.accessRestriction || this.configuration.common.logPath)){
			if(!this.logKey){
				this.logKey = this.generateRandomHash(40);
			}
			var videoElement = $('.sivaPlayer_mainVideo', this.player);
			var offset = 0;
			if(videoElement.length > 0){
				offset = this.currentSceneTime;
			}
			this.log.push({
				'id': this.logCounter,
				'timeOffset': (new Date()).getTime() - this.initTime,
				'sceneOffset': offset,
				'type': type,
				'element': element,
				'extraInfo': extraInfo,
				'clientTime': (new Date()).getTime()
			});
			try{
				sivaPlayerStorage.set('sivaPlayerLog_' + this.logKey, JSON.stringify({
					'logPath': ((this.configuration.common.logPath) ? this.configuration.common.logPath : this.configuration.videoPath),
					'logKey': this.logKey,
					'accessToken': ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) ? this.configuration.accessRestriction.accessToken : ''),
					'email': ((this.configuration.common.userEmail) ? this.configuration.common.userEmail : ''),
					'secret': ((this.configuration.common.userSecret) ? this.configuration.common.userSecret : ''),
					'log': this.log
				}));
			}catch(e){}
			this.logCounter++;
		}
	};
	
	this.logClientInformation = function(){
		if(window.navigator){
			if(window.navigator.userAgent){
				this.logAction('getClientInformation', 'userAgent', window.navigator.userAgent);
			}
			if(window.navigator.platform){
				this.logAction('getClientInformation', 'platform', window.navigator.platform);
			}
		}
		if(window.screen){
			this.logAction('getClientInformation', 'resolution', window.screen.width + 'x' + window.screen.height);
		}
	};
	
	this.syncLog = function(){
		var thisPlayer = this;
		if(this.configuration.common.log && ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) || (this.configuration.common.userEmail && this.configuration.common.userSecret))){
			if(window.navigator && window.navigator.onLine && this.log.length > 0){
				var counter = this.logCounter;
				$.ajax({
					'async': true,
					'data': {'ajax': 'true', 'data': JSON.stringify(this.log)},
					'dataType': 'JSON',
					'crossDomain': true,
					'timeout': 60000,
					'type': 'POST',
					'url': ((this.configuration.common.logPath) ? this.configuration.common.logPath : this.configuration.videoPath) + '/log?token=' + ((this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken) ? this.configuration.accessRestriction.accessToken : '&token2=' + this.logKey) + '&email=' + ((this.configuration.common.userEmail) ? this.configuration.common.userEmail : '') + '&secret=' + ((this.configuration.common.userSecret) ? this.configuration.common.userSecret : ''),
					'error': function(){},
					'success': function(data){
						if(data.logged == 'true'){
							for(var i = 0; i < counter && thisPlayer.log.length > 0 && thisPlayer.log[0].id < counter; i++){
								thisPlayer.log.splice(0, 1);
							}
							if(thisPlayer.log.length == 0){
								sivaPlayerStorage.remove('sivaPlayerLog_' + thisPlayer.logkey);
							}
							else{
								try{
									sivaPlayerStorage.set('sivaPlayerLog_' + thisPlayer.logkey, JSON.stringify({
										'logPath': ((thisPlayer.configuration.common.logPath) ? thisPlayer.configuration.common.logPath : thisPlayer.configuration.videoPath),
										'logKey': thisPlayer.logKey,
										'accessToken': ((thisPlayer.configuration.accessRestriction && thisPlayer.configuration.accessRestriction.accessToken) ? thisPlayer.configuration.accessRestriction.accessToken : ''),
										'email': ((thisPlayer.configuration.common.userEmail) ? thisPlayer.configuration.common.userEmail : ''),
										'secret': ((thisPlayer.configuration.common.userSecret) ? thisPlayer.configuration.common.userSecret : ''),
										'log': thisPlayer.log
									}));
								}catch(e){}
							}
						}
						else{
							thisPlayer.throwError(new Error(thisPlayer.getLabel('logSyncError') + ' ' + thisPlayer.getLabel(data.description)), true);
						}
						if(data.disabledTitle && data.disabledText){
							sivaPlayerStorage.set('disabledTitle', data.disabledTitle);
							sivaPlayerStorage.set('disabledText', data.disabledText);
						}
					}
				});
			}
		}
		if(this.configuration.common.log){
			setTimeout(function(){
				thisPlayer.syncLog();
			}, 65000);
		}
	};
	
	this.generateRandomHash = function(length){
		var possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		var hash = '';
		for( var i=0; i < length; i++ ){
	        hash += possibleCharacters.charAt(Math.floor(Math.random() * possibleCharacters.length));
		}
	    return hash;	
	};
	
	this.throwError = function(error, isInnerFunction){
		this.active = false;
		this.logAction('showError', 'customError', error.message + ((error.fileName) ? ' (' + error.fileName + ':' + error.lineNumber + ':' + error.columnNumber + ')' : ''));
		var exception = new SivaPlayerFatalException(this.player, error, true);
		if(isInnerFunction){
			exception.showMessage();
		}
		else{
			throw exception;
		}
	};
};

function SivaPlayerStorage(){
	
	this.hasLocalStorage;
	
	this.hasChromeLocalStorage;
	
	this.init = function(){
		this.hasLocalStorage = !(typeof localStorage == 'undefined');
		this.hasChromeLocalStorage = !(typeof chrome == 'undefined' || !chrome.storage || !chrome.storage.local);
	};
	
	this.get = function(key, callback){
		if(this.hasChromeLocalStorage){
			chrome.storage.local.get(key, function(result){
				callback(result[key]);
			});
		}
		else if(this.hasLocalStorage){
			callback(localStorage[key]);
		}
	};
	
	this.getAll = function(callback){
		if(this.hasChromeLocalStorage){
			chrome.storage.local.get(callback);
		}
		else if(this.hasLocalStorage){
			callback(localStorage);
		}
	};
	
	this.set = function(key, value){
		if(this.hasChromeLocalStorage){
			var update = {};
			update[key] = value;
			chrome.storage.local.set(update, function(){});
		}
		else if(this.hasLocalStorage){
			localStorage[key] = value;
		}
	};
	
	this.remove = function(key){
		if(this.hasChromeLocalStorage){
			chrome.storage.local.remove(key);
		}
		else if(this.hasLocalStorage){
			delete localStorage[key];
		}
	};
	
	this.clear = function(){
		if(this.hasChromeLocalStorage){
			chrome.storage.local.clear();
		}
		else if(this.hasLocalStorage){
			delete localStorage.clear();
		}
	};
}

function SivaPlayerFatalException(player, error, customException){	
	this.player = player;	
	this.error = error;	
	this.showMessage = function(){
		$(player)
			.empty()
			.append('<div class="sivaPlayer_popup sivaPlayer_fatalError' + ((!customException) ? ' sivaPlayer_browserError' : '') + '"><table><tr><td class="sivaPlayer_title">An error occurred</td></tr><tr><td class="sivaPlayer_content"><div class="sivaPlayer_scrollable">' + this.error.message + ((this.error.fileName) ? '<br />in <i>' + this.error.fileName + ':' + this.error.lineNumber + ':' + this.error.columnNumber + '</i>' : '') + '</div></td></tr></table></div>');
	};
}

function sivaPlayerClearLog(callback){
	if(window.navigator && window.navigator.onLine){
		sivaPlayerStorage.getAll(function(result){
			var keys = [];
			for(var key in result){
				if(key.indexOf('sivaPlayerLog_') == 0){
					keys.push(key);
				}
			}
			sivaPlayerClearLogHelper(keys, result, callback);
			if(keys.length == 0 && callback){
				callback('nothingToSyncError', '');
			}
		});
	}
	else if(callback){
		callback('notOnlineError', '');
	}
}

function sivaPlayerClearLogHelper(keys, log, callback){
	if(keys.length > 0){
		var key = keys.shift();
		var entry = JSON.parse(log[key]);
		$.ajax({
			'async': true,
			'data': {'ajax': 'true', 'data': JSON.stringify(entry.log)},
			'dataType': 'JSON',
			'crossDomain': true,
			'timeout': 60000,
			'type': 'POST',
			'url': entry.logPath + '/log?token=' + entry.accessToken + ((entry.accessToken == '') ? '&token2=' + entry.logKey : '') + '&email=' + entry.email + '&secret=' + entry.secret,
			'error': function(e){
				if(callback){
					callback('', e.responseText);
				}
			},
			'success': function(data){
				if(data.logged == 'true'){
					sivaPlayerStorage.remove(key);
					sivaPlayerClearLogHelper(keys, log, callback);
				}
				else{
					if(callback){
						callback('dataNotSyncedError', '');
					}
				}
			}
		});
	}
	else{
		if(callback){
			callback('successfullySyncedMessage', '');
		}
	}
}

var sivaPlayerArray = [];
var sivaPlayerStorage;
$(document).ready(function(){
	sivaPlayerStorage = new SivaPlayerStorage();
	sivaPlayerStorage.init();
	setTimeout(function(){
		sivaPlayerClearLog();
	}, 0);
	$.each($('.sivaPlayer'), function(){
		var player = new SivaPlayer(this, sivaPlayerArray.length);
		sivaPlayerArray.push(player);	
		try {
			player.init();
		}
		catch(e){
			$(player.player).fadeIn(800);
			if(e instanceof SivaPlayerFatalException){
				e.showMessage();
			}
			else{
				player.active = false;
				player.logAction('showError', 'customError', e.message + ((e.fileName) ? ' (' + e.fileName + ':' + e.lineNumber + ':' + e.columnNumber + ')' : ''));
				var exception = new SivaPlayerFatalException(player.player, e, false, 'An error occurred');
				exception.showMessage();
			}
		}
	});
});

var sivaPlayerFragments = $.param.fragment();
$(window).bind("hashchange", function(e){
	var tmpFragments = sivaPlayerFragments.split('&');
	var previousFragments = {};
	for(var i = 0; i < tmpFragments.length; i++){
		var fragment = tmpFragments[i].split('=');
		previousFragments[(fragment[0])] = fragment[1];
	}
	
	var found = false;
	tmpFragments = $.param.fragment().split('&');
	var currentFragments = {};
	for(var i = 0; i < tmpFragments.length; i++){
		var fragment = tmpFragments[i].split('=');
		if(previousFragments[(fragment[0])] != fragment[1]){
			var player = sivaPlayerArray[(fragment[0])];
			player.setNextNode(fragment[1], true);
			found = true;
			break;
		}
		currentFragments[(fragment[0])] = fragment[1];
	}
	
	if(!found){
		for(var i in previousFragments){
			if(previousFragments[i] != currentFragments[i]){
				var player = sivaPlayerArray[i];
				player.setNextNode(undefined, true);
				break;
			}
		}
	}
	
	sivaPlayerFragments = $.param.fragment();
});

$(window).trigger('hashchange')
.resize(function(){
	for(var i = 0; i < sivaPlayerArray.length; i++){
		sivaPlayerArray[i].onResize();
	}
})
.unload(function(){
	for(var i = 0; i < sivaPlayerArray.length; i++){		
		sivaPlayerArray[i].logAction('leaveVideo', '', '');
	}
	setTimeout(function(){
		sivaPlayerClearLog();
	}, 0);
});
$(document).on("webkitfullscreenchange mozfullscreenchange fullscreenchange",function(){
	for(var i = 0; i < sivaPlayerArray.length; i++){
		sivaPlayerArray[i].setFullscreenButton();
	}
});