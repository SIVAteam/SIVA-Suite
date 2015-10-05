var SIVA_PLAYER_DEFAULT_PRIMARY_COLOR = '#1173AA';
var SIVA_PLAYER_DEFAULT_SECONDARY_COLOR = '#ffffff';
var SIVA_PLAYER_ALLOWED_COMMUNITY_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'pdf'];
var SIVA_PLAYER_ALLOWED_COMMUNITY_MAX_FILESIZE = 10485760;

function SivaPlayer(DOMObject, arrayPosition){
	this.player = DOMObject;
	
	this.arrayPosition = arrayPosition;
		
	this.configuration = {};
	
	this.active = true;
	
	this.playedBefore = [];
	
	this.currentScene;
	
	this.currentSceneTime = 0;
	
	this.currentSceneEnded = false;
	
	this.lastTimeObject = {};
	
	this.volume = 1;
	
	this.previousVolume = -1;
	
	this.volumeSetByUser = true;
	
	this.history = [];
	
	this.sceneHistory = {};
	
	this.isAnnotationSidebarVisible = false;
	
	this.isAnnotationVisible = true;
	
	this.isSubTitleVisible = true;
	
	this.visibleSidebarAnnotationType = 'all';
	
	this.hashchangeCounter = 0;
	
	this.currentLanguage;
	
	this.log = [];
	
	this.logKey;
	
	this.logCounter = 0;
	
	this.initTime = 0;
	
	this.isMobile = false;
	
	this.playingMediaAnnotations = [];
	
	this.communityAnnotations = [];
	
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
				if(result.visibleSidebarAnnotationType){
					thisPlayer.visibleSidebarAnnotationType = result.visibleSidebarAnnotationType;
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
					if(thisPlayer.configuration.common.isChromecastReceiver){
						$(thisPlayer.player).addClass('sivaPlayer_chromecastReceiver');
					}
					thisPlayer.initTime = (new Date()).getTime();
					if(sivaPlayerStorage.hasChromeLocalStorage){
						thisPlayer.logAction('getClientInformation', 'chromeApp', '');
					}
					thisPlayer.checkIncludedLanguages();
					thisPlayer.createCustomStyles();
					var startScene = $.bbq.getState(thisPlayer.arrayPosition);
					if(!startScene)
						startScene = thisPlayer.configuration.startScene;
					thisPlayer.setNextNode(startScene, thisPlayer.configuration.common.autoStart && !thisPlayer.configuration.common.isChromecastReceiver);
					thisPlayer.hideControls();
					thisPlayer.logClientInformation();
					thisPlayer.syncLog();
				}
			});
		}
	};
	
	this.preparePlayer = function(){
		var thisPlayer = this;
		var lastPosition = {'x': 0, 'y': 0};
		FastClick.attach(this.player);
		$(this.player).addClass('sivaPlayer_arrayPosition' +  this.arrayPosition)
		.mousemove(function(e){
			if(e.clientX != lastPosition.x || e.clientY != lastPosition.y){
				lastPosition.x = e.clientX;
				lastPosition.y = e.clientY;
				thisPlayer.showControls();
			}
		});
		$(document).unbind('keyup').keyup(function(e){
			if(e.keyCode == '27'){
				if(!document.fullScreen && !document.mozFullScreen && !document.webkitIsFullScreen){
					if($('.sivaPlayer_popup .sivaPlayer_closeButton, .sivaPlayer_searchSidebar, .sivaPlayer_annotationEditor', thisPlayer.player).length > 0){
						thisPlayer.logAction('useKey', 'ESC', '');
						thisPlayer.closePopups(600, true, true);
					}
				}
			}
			else if(e.keyCode == '32'){
				if($(e.target).prop('tagName') == 'INPUT' || $(e.target).prop('tagName') == 'TEXTAREA'){
					return;
				}
				else if($('.sivaPlayer_popup.sivaPlayer_mediaPopup', thisPlayer.player).length > 0){
					var mediaElement = $('.sivaPlayer_mediaPopup video, .sivaPlayer_mediaPopup audio', thisPlayer.player)[0];
					thisPlayer.logAction('useKey', 'SPACE', ((mediaElement.paused) ? 'play' : 'pause'));
					if(mediaElement.paused){
						mediaElement.play();
					}
					else{
						mediaElement.pause();
					}				
				}
				else if($('.sivaPlayer_popup .sivaPlayer_closeButton, .sivaPlayer_searchSidebar, .sivaPlayer_annotationEditor', thisPlayer.player).length > 0){
					thisPlayer.logAction('useKey', 'SPACE', '');
					thisPlayer.closePopups(600);
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
			if($(e.target).prop('tagName') == 'INPUT' || $(e.target).prop('tagName') == 'TEXTAREA'){
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
			$('.sivaPlayer_loader .sivaPlayer_text', this.player).text(this.getLabel('loading'));
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
		if(/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Nokia|Symbian|Kindle|Silk|Opera Mini/i.test(window.navigator.userAgent) && !this.phonegap){
			this.isMobile = true;
		}
	};
	
	this.setConfiguration = function(config, noUserOverwrites){				
		var thisPlayer = this;
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
		
		this.isAnnotationSidebarVisible = (this.configuration.common && (this.configuration.common.annotationSidebarVisibility == 'always' || this.configuration.common.annotationSidebarVisibility == 'onStart'));
		this.isAnnotationSidebarVisibleUserSelection = this.isAnnotationSidebarVisible;
		
		if(!this.configuration.accessRestriction || !this.configuration.accessRestriction.accessToken){
			this.configuration.common.collaboration = false;
		}
		
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
		var css = [{'property': 'background', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_videoContainer .sivaPlayer_annotation audio', '.sivaPlayer_loading .sivaPlayer_loadingDot', '.sivaPlayer_popup', '.sivaPlayer_controls', '.sivaPlayer_volumeControl', '.sivaPlayer_annotationSidebar', '.sivaPlayer_nodeSelectionSidebar', '.sivaPlayer_searchSidebar', '.sivaPlayer_annotationEditor', '.sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_markerButton span', '.sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timelineMarker', '.sivaPlayer_mediaAnnotationControls .sivaPlayer_timeline .sivaPlayer_timelineProgress span span', '.sivaPlayer_annotationSidebarButton span', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_hover', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_hover', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation:hover', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation:hover', '.sivaPlayer_annotationEditor .sivaPlayer_visibility select', '.sivaPlayer_communityPopup .sivaPlayer_answers textarea', '.sivaPlayer_communityPopup .sivaPlayer_answers button', '.sivaPlayer_portrait .sivaPlayer_annotationEditor input', '.sivaPlayer_landscape .sivaPlayer_annotationEditor input', '.sivaPlayer_portrait .sivaPlayer_annotationEditor textarea', '.sivaPlayer_landscape .sivaPlayer_annotationEditor textarea', '.sivaPlayer_portrait .sivaPlayer_annotationEditor button', '.sivaPlayer_landscape .sivaPlayer_annotationEditor button', '.sivaPlayer_portrait .sivaPlayer_annotationEditor .sivaPlayer_media svg', '.sivaPlayer_landscape .sivaPlayer_annotationEditor .sivaPlayer_media svg', '.sivaPlayer_communityPopup .sivaPlayer_newAnswer .sivaPlayer_addMedia svg', '.sivaPlayer_portrait .sivaPlayer_tableOfContentsPopup table', '.sivaPlayer_landscape .sivaPlayer_tableOfContentsPopup table', '.sivaPlayer_mediaPopup .sivaPlayer_content', '.sivaPlayer_portrait .sivaPlayer_annotation .sivaPlayer_activation', '.sivaPlayer_landscape .sivaPlayer_annotation .sivaPlayer_activation', '.sivaPlayer_communityPopup .sivaPlayer_activation']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_popup label input', '.sivaPlayer_popup label button', '.sivaPlayer_searchSidebar input', '.sivaPlayer_annotationEditor textarea', '.sivaPlayer_sceneList a', '.sivaPlayer_timeline .sivaPlayer_timelineProgress span span', '.sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timlineUpdateSelectedTime', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations', '.sivaPlayer_mediaPopup .sivaPlayer_content .sivaPlayer_mediaAnnotationControls', '.sivaPlayer_videoContainer .sivaPlayer_annotation .sivaPlayer_title', '.sivaPlayer_videoContainer .sivaPlayer_richtextAnnotation', '.sivaPlayer_volumeControl .sivaPlayer_volume.sivaPlayer_current', '.sivaPlayer_popup.sivaPlayer_zoomPopup', '.sivaPlayer_popup.sivaPlayer_mediaPopup', '.sivaPlayer_popup.sivaPlayer_pdfPopup', '.sivaPlayer_popup.sivaPlayer_richtextPopup', '.sivaPlayer_popup.sivaPlayer_communityPopup', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar', '.sivaPlayer_sidebarTabs span.sivaPlayer_hover', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation:hover', '.sivaPlayer_annotationEditor input', '.sivaPlayer_annotationEditor .sivaPlayer_media svg', '.sivaPlayer_annotationEditor button', '.sivaPlayer_portrait .sivaPlayer_annotationEditor', '.sivaPlayer_landscape .sivaPlayer_annotationEditor', '.sivaPlayer_portrait .sivaPlayer_annotationEditor .sivaPlayer_visibility select', '.sivaPlayer_landscape .sivaPlayer_annotationEditor .sivaPlayer_visibility select', '.sivaPlayer_annotation .sivaPlayer_activation']},{'value': 'rgba(' + this.hexToRgb(primaryColor) + ', 0.7)', 'selectors': ['.sivaPlayer_portrait .sivaPlayer_sidebarTabs span', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span']},{'value': 'rgba(' + this.hexToRgb(primaryColor) + ', 0.3)', 'selectors': ['.sivaPlayer_portrait .sivaPlayer_videoAnnotation .sivaPlayer_placeholder', '.sivaPlayer_portrait .sivaPlayer_audioAnnotation .sivaPlayer_placeholder', '.sivaPlayer_landscape .sivaPlayer_videoAnnotation .sivaPlayer_placeholder', '.sivaPlayer_landscape .sivaPlayer_audioAnnotation .sivaPlayer_placeholder']},{'value': 'rgba(' + this.hexToRgb(secondaryColor) + ', 0.7)', 'selectors': ['.sivaPlayer_volumeControl .sivaPlayer_volume.sivaPlayer_hover', '.sivaPlayer_sidebarTabs span']},{'value': 'rgba(' + this.hexToRgb(secondaryColor) + ', 0.5)', 'selectors': ['.sivaPlayer_videoContainer .sivaPlayer_galleryAnnotation']},{'value': 'rgba(' + this.hexToRgb(secondaryColor) + ', 0.2)', 'selectors': ['.sivaPlayer_videoAnnotation .sivaPlayer_placeholder', '.sivaPlayer_audioAnnotation .sivaPlayer_placeholder']}]},
					 {'property': 'color', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_accessRestrictionPopup label input', '.sivaPlayer_accessRestrictionPopup label button', '.sivaPlayer_searchSidebar input', '.sivaPlayer_annotationEditor textarea', '.sivaPlayer_sceneList a', '.sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timlineUpdateSelectedTime', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations', '.sivaPlayer_mediaPopup .sivaPlayer_content .sivaPlayer_mediaAnnotationControls', '.sivaPlayer_popup label input', '.sivaPlayer_popup label button', '.sivaPlayer_popup.sivaPlayer_zoomPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_mediaPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_pdfPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_richtextPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_communityPopup .sivaPlayer_title', '.sivaPlayer_videoContainer .sivaPlayer_annotation', '.sivaPlayer_videoContainer .sivaPlayer_annotation a', '.sivaPlayer_volumeControl .sivaPlayer_volume.sivaPlayer_current', '.sivaPlayer_volumeControl .sivaPlayer_volume.sivaPlayer_hover', '.sivaPlayer_popup.sivaPlayer_zoomPopup', '.sivaPlayer_popup.sivaPlayer_mediaPopup', '.sivaPlayer_popup.sivaPlayer_pdfPopup', '.sivaPlayer_popup.sivaPlayer_richtextPopup', '.sivaPlayer_popup.sivaPlayer_communityPopup', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar', '.sivaPlayer_richtextPopup a', '.sivaPlayer_communityPopup a', '.sivaPlayer_sidebarTabs span', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation:hover', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation', '.sivaPlayer_annotationEditor input', '.sivaPlayer_annotationEditor button', '.sivaPlayer_portrait .sivaPlayer_annotationEditor', '.sivaPlayer_landscape .sivaPlayer_annotationEditor', '.sivaPlayer_portrait .sivaPlayer_annotationEditor .sivaPlayer_visibility select', '.sivaPlayer_landscape .sivaPlayer_annotationEditor .sivaPlayer_visibility select', '.sivaPlayer_annotation .sivaPlayer_activation']}, {'value': secondaryColor, 'selectors': ['', '.sivaPlayer_searchSidebar .sivaPlayer_results a', '.sivaPlayer_popup', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations.sivaPlayer_active', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations.sivaPlayer_active', '.sivaPlayer_tableOfContents a', '.sivaPlayer_tableOfContents span', '.sivaPlayer_markerButton span', '.sivaPlayer_annotation a', '.sivaPlayer_readMoreLink', '.sivaPlayer_popup .sivaPlayer_title', '.sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span', '.sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation:hover', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation:hover', '.sivaPlayer_annotationEditor .sivaPlayer_visibility select', '.sivaPlayer_communityPopup .sivaPlayer_answers textarea', '.sivaPlayer_communityPopup .sivaPlayer_answers button', '.sivaPlayer_portrait .sivaPlayer_annotationEditor input', '.sivaPlayer_landscape .sivaPlayer_annotationEditor input', '.sivaPlayer_portrait .sivaPlayer_annotationEditor textarea', '.sivaPlayer_landscape .sivaPlayer_annotationEditor textarea', '.sivaPlayer_portrait .sivaPlayer_annotationEditor button', '.sivaPlayer_landscape .sivaPlayer_annotationEditor button', '.sivaPlayer_portrait .sivaPlayer_annotation .sivaPlayer_activation', '.sivaPlayer_landscape .sivaPlayer_annotation .sivaPlayer_activation', '.sivaPlayer_communityPopup .sivaPlayer_activation']}]},
					 {'property': 'fill', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_mediaAnnotationControls .sivaPlayer_button svg', '.sivaPlayer_popup.sivaPlayer_zoomPopup .sivaPlayer_closeButton svg', '.sivaPlayer_popup.sivaPlayer_pdfPopup .sivaPlayer_closeButton svg', '.sivaPlayer_popup.sivaPlayer_mediaPopup .sivaPlayer_closeButton svg', '.sivaPlayer_popup.sivaPlayer_richtextPopup .sivaPlayer_closeButton svg', '.sivaPlayer_popup.sivaPlayer_communityPopup .sivaPlayer_closeButton svg', '.sivaPlayer_overlayButton svg polygon', '.sivaPlayer_overlayButton svg path', '.sivaPlayer_statsPopup svg .day', '.sivaPlayer_button.sivaPlayer_galleryPreviousButton', '.sivaPlayer_button.sivaPlayer_galleryNextButton', '.sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation:hover svg rect', '.sivaPlayer_portrait .sivaPlayer_pdfAnnotation svg polygon', '.sivaPlayer_landscape .sivaPlayer_pdfAnnotation svg polygon', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation svg rect', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation svg rect', '.sivaPlayer_annotationEditor .sivaPlayer_media svg', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar .sivaPlayer_error svg', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar .sivaPlayer_error svg']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_button', '.sivaPlayer_button svg', '.sivaPlayer_tableOfContents svg', '.sivaPlayer_overlayButton svg circle', '.sivaPlayer_button.sivaPlayer_galleryPreviousButton circle', '.sivaPlayer_button.sivaPlayer_galleryNextButton circle', '.sivaPlayer_statsPopup svg text', '.sivaPlayer_popup .sivaPlayer_closeButton svg', '.sivaPlayer_pdfAnnotation svg polygon', '.sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation svg rect', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation:hover svg rect', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation:hover svg rect', '.sivaPlayer_portrait .sivaPlayer_annotationEditor .sivaPlayer_media svg', '.sivaPlayer_landscape .sivaPlayer_annotationEditor .sivaPlayer_media svg', '.sivaPlayer_communityPopup .sivaPlayer_newAnswer .sivaPlayer_addMedia svg']}, {'value': 'rgba(' + this.hexToRgb(secondaryColor) + ', 0.2)', 'selectors': ['.sivaPlayer_markerEllipse', '.sivaPlayer_markerRectangle', '.sivaPlayer_markerPolygon']}]},
					 {'property': 'border-color', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timelineUpdate span', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations', '.sivaPlayer_mediaAnnotationControls .sivaPlayer_timeline .sivaPlayer_timelineProgress', '.sivaPlayer_annotationSidebarButton', '.sivaPlayer_popup.sivaPlayer_zoomPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_mediaPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_pdfPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_richtextPopup .sivaPlayer_title', '.sivaPlayer_popup.sivaPlayer_communityPopup .sivaPlayer_title', '.sivaPlayer_sidebarTabs span', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation', '.sivaPlayer_portrait .sivaPlayer_annotationEditor .sivaPlayer_visibility select', '.sivaPlayer_landscape .sivaPlayer_annotationEditor .sivaPlayer_visibility select']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_sceneList a', '.sivaPlayer_timeline .sivaPlayer_timelineProgress', '.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations.sivaPlayer_active', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations.sivaPlayer_active', '.sivaPlayer_sidebarTabs .sivaPlayer_spacer', '.sivaPlayer_annotationSidebar > div.sivaPlayer_active .sivaPlayer_annotation', '.sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timelineMarker', '.sivaPlayer_mediaAnnotationControls .sivaPlayer_timeline .sivaPlayer_timelineProgress .sivaPlayer_timelineUpdate span', '.sivaPlayer_volumeControl .sivaPlayer_volume', '.sivaPlayer_popup .sivaPlayer_title', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span', '.sivaPlayer_annotationSidebar .sivaPlayer_newCommunityAnnotation', '.sivaPlayer_annotationEditor .sivaPlayer_visibility select']}]},
					 {'property': 'border-top-color', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_communityPopup .sivaPlayer_answers div', '.sivaPlayer_portrait .sivaPlayer_annotationEditor .sivaPlayer_duration', '.sivaPlayer_portrait .sivaPlayer_annotationEditor .sivaPlayer_visibility', '.sivaPlayer_landscape .sivaPlayer_annotationEditor .sivaPlayer_duration', '.sivaPlayer_landscape .sivaPlayer_annotationEditor .sivaPlayer_visibility']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_annotationEditor .sivaPlayer_duration', '.sivaPlayer_annotationEditor .sivaPlayer_visibility', '.sivaPlayer_portrait .sivaPlayer_annotationEditor']}]},
					 {'property': 'border-bottom-color', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations.sivaPlayer_active', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations.sivaPlayer_active', '.sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar > div.sivaPlayer_active .sivaPlayer_annotation', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar > div.sivaPlayer_active .sivaPlayer_annotation']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_sidebarTabs .sivaPlayer_authorAnnotations', '.sivaPlayer_sidebarTabs .sivaPlayer_communityAnnotations', '.sivaPlayer_sidebarTabs span', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_active', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_active.sivaPlayer_hover']}, {'value': '1px solid rgba(' + this.hexToRgb(secondaryColor) + ', 0.3)', 'selectors': ['.sivaPlayer_searchSidebar .sivaPlayer_results a']}]},
					 {'property': 'border-left-color', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_communityAnnotationTab.sivaPlayer_active', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_communityAnnotationTab.sivaPlayer_active', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_allAnnotationTab.sivaPlayer_active', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_allAnnotationTab.sivaPlayer_active']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_sidebarTabs span.sivaPlayer_communityAnnotationTab.sivaPlayer_active', '.sivaPlayer_sidebarTabs span.sivaPlayer_allAnnotationTab.sivaPlayer_active']}]},
					 {'property': 'border-right-color', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_authorAnnotationTab.sivaPlayer_active', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_authorAnnotationTab.sivaPlayer_active', '.sivaPlayer_portrait .sivaPlayer_sidebarTabs span.sivaPlayer_allAnnotationTab.sivaPlayer_active', '.sivaPlayer_landscape .sivaPlayer_sidebarTabs span.sivaPlayer_allAnnotationTab.sivaPlayer_active']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_sidebarTabs span.sivaPlayer_authorAnnotationTab.sivaPlayer_active', '.sivaPlayer_sidebarTabs span.sivaPlayer_allAnnotationTab.sivaPlayer_active']}]},
					 {'property': 'stroke', 'values': [{'value': primaryColor, 'selectors': ['.sivaPlayer_markerEllipse', '.sivaPlayer_markerRectangle', '.sivaPlayer_markerPolygon', '.sivaPlayer_portrait .sivaPlayer_communityAnnotation .sivaPlayer_info span svg path', '.sivaPlayer_landscape .sivaPlayer_communityAnnotation .sivaPlayer_info span svg path', '.sivaPlayer_communityPopup .sivaPlayer_media svg', '.sivaPlayer_portrait .sivaPlayer_annotationSidebar .sivaPlayer_error svg', '.sivaPlayer_landscape .sivaPlayer_annotationSidebar .sivaPlayer_error svg']}, {'value': secondaryColor, 'selectors': ['.sivaPlayer_statsButton svg line', '.sivaPlayer_statsButton svg line', '.sivaPlayer_statsPopup svg .day', '.sivaPlayer_statsPopup svg .month', '.sivaPlayer_communityAnnotation .sivaPlayer_info span svg path']}]}
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
		if($('.sivaPlayer_controls', this.player).length > 0){
			this.showControls();
		}
		if($('.sivaPlayer_statsPopup', this.player).length > 0){
			this.createStats();
		}
		this.setProportions();
		if($('.sivaPlayer_mainVideo', this.player).length > 0 && (!this.configuration.accessRestriction || this.configuration.accessRestriction.passed)){
			this.updateAnnotations();
		}
	};
	
	this.setProportions = function(){
		var thisPlayer = this;
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
			this.mode = 'default';
			if($(this.player).is('.sivaPlayer_portrait, .sivaPlayer_landscape') && this.isAnnotationSidebarVisible){
				this.slideInAnnotationSidebar();
			}
			$(this.player).removeClass('sivaPlayer_portrait').removeClass('sivaPlayer_landscape');
		}
		var video = $('.sivaPlayer_mainVideo', this.player);
		if(video.length > 0){
			$('.sivaPlayer_videoContainer', this.player).css(this.getVideoContainerProportions(video[0], playerWidth, playerHeight));
			var volumeButton = $('.sivaPlayer_controls .sivaPlayer_volumeButton', this.player);
			if(volumeButton.length > 0){
				var safari = navigator.userAgent.split('Safari/');
				if(safari.length > 1 && safari[1].split('.')[0] < 536){
					playerWidth -= 5;
				}
				$('.sivaPlayer_controls .sivaPlayer_volumeControl', this.player).css('left', ($(volumeButton[0]).offset().left - playerOffset.left - 5) + 'px');
			}
			$.each($('.sivaPlayer_overlayButton', this.player), function(){
				var parent = $(this).parent();
				var tmpWidth = parseInt($(parent).width() * 0.3);
				var tmpHeight = parseInt($(parent).height() * 0.4);
				if(tmpHeight < tmpWidth)
					tmpWidth = tmpHeight;
				else
					tmpHeight = tmpWidth;
				$(this).css({'width': tmpWidth + 'px', 'height': tmpHeight + 'px', 'margin-top': parseInt(tmpHeight / -2) + 'px', 'margin-left': parseInt(tmpWidth / -2) + 'px'});
			});
		}
		var sidebar = $('.sivaPlayer_annotationSidebar', this.player);
		if(sidebar.length > 0){
			setTimeout(function(){
				$('.sivaPlayer_annotationSidebar .sivaPlayer_scrollHolder', thisPlayer.player).css('height', ($(sidebar).height() - $('.sivaPlayer_sidebarTabs', sidebar).height()) + 'px');
			}, 200);
			$('.sivaPlayer_annotationEditor', this.player).css('width', parseInt(this.configuration.style.annotationSidebarWidth * playerWidth + 40) + 'px');
		}
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
			tmpHeight = tmpHeight - $('.sivaPlayer_zoomPopup .sivaPlayer_title', this.player).height() - parseInt($('.sivaPlayer_zoomPopup .sivaPlayer_title', this.player).css('padding-top')) - parseInt($('.sivaPlayer_zoomPopup .sivaPlayer_title', this.player).css('padding-bottom')) - parseInt($('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).css('padding-bottom')) - parseInt($('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).css('padding-top'));
			$('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).css({'height': tmpHeight, 'width': tmpWidth});
			$('.sivaPlayer_zoomPopup tr.sivaPlayer_galleryThumbnails div', this.player).width(playerWidth + 'px');
			$('.sivaPlayer_zoomPopup tr.sivaPlayer_galleryThumbnails td', this.player).height(parseInt(playerHeight * 0.15) + 'px');
			$('.sivaPlayer_zoomPopup .sivaPlayer_content img', this.player).css({
				'max-height': tmpHeight + 'px',
				'max-width': tmpWidth + 'px'
			});
			var buttons = $('.sivaPlayer_zoomPopup .sivaPlayer_galleryPreviousButton, .sivaPlayer_zoomPopup .sivaPlayer_galleryNextButton', this.player);
			$(buttons).css('margin-top', parseInt($(buttons).height() / -2) + 'px');
		}
		if($('.sivaPlayer_mediaPopup .sivaPlayer_content', this.player).length > 0){
			$('.sivaPlayer_mediaPopup .sivaPlayer_content video', this.player).height(playerHeight - $('.sivaPlayer_mediaPopup .sivaPlayer_title', this.player).height() - parseInt($('.sivaPlayer_mediaPopup .sivaPlayer_title', this.player).css('padding-top')) - parseInt($('.sivaPlayer_mediaPopup .sivaPlayer_title', this.player).css('padding-bottom')));
		}
		if($('.sivaPlayer_pdfPopup .sivaPlayer_content', this.player).length > 0){
			$('.sivaPlayer_pdfPopup .sivaPlayer_content', this.player).height(playerHeight - $('.sivaPlayer_pdfPopup .sivaPlayer_title', this.player).height() - parseInt($('.sivaPlayer_pdfPopup .sivaPlayer_title', this.player).css('padding-top')) - parseInt($('.sivaPlayer_pdfPopup .sivaPlayer_title', this.player).css('padding-bottom')));
		}
		if($('.sivaPlayer_annotationSidebar .sivaPlayer_timeline, .sivaPlayer_videoContainer .sivaPlayer_timeline', this.player).width() < 80){
			$('.sivaPlayer_contronls .sivaPlayer_timeline', this.player).addClass('sivaPlayer_reducedTimeline');
		}
		else{
			$('.sivaPlayer_contronls .sivaPlayer_timeline', this.player).removeClass('sivaPlayer_reducedTimeline');
		}
		if(this.configuration.style){
			$('.sivaPlayer_searchSidebar', this.player).css('width', parseInt(this.configuration.style.annotationSidebarWidth * $(this.player).width() + 40) + 'px');
		}
	};
	
	this.getVideoContainerProportions = function(videoElement, playerWidth, playerHeight){
		var thisPlayer = this;
		var videoWidth = videoElement.videoWidth;
		var videoHeight = videoElement.videoHeight;
		var containerWidth = playerWidth;
		var containerHeight = playerHeight;
		if(this.mode == 'default' && this.isAnnotationSidebarVisible && $('.sivaPlayer_annotationSidebar', this.player).length > 0 && !this.configuration.common.annotationSidebarOverlay){
			containerWidth -= thisPlayer.configuration.style.annotationSidebarWidth * playerWidth;
		}
		else if(this.mode == 'landscape'){
			containerWidth *= 0.5;
		}
		else if(this.mode == 'portrait'){
			containerHeight *= 0.5;
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
		else if(this.configuration.common && this.configuration.common.useSecretLogin && this.configuration.common.userEmail != '' && this.configuration.common.userSecret != ''){
			this.startLoader();
			$('.sivaPlayer_accessRestrictionPopup', thisPlayer.player).remove();
			$.ajax({
				'async': true,
				'data': 'ajax=true&username=' + this.configuration.common.userEmail + '&secret=' + this.configuration.common.userSecret,
				'dataType': 'JSON',
				'crossDomain': true,
				'timeout': 60000,
				'type': 'POST',
				'url': this.configuration.configPath,
				'error': function(){
					thisPlayer.throwError(new Error(thisPlayer.getLabel('videoAccessError')), true);
				},
				'success': function(data){
					if(data.accessToken){
						data.additionalInformation = thisPlayer.configuration.accessRestriction.additionalInformation;
						thisPlayer.configuration.accessRestriction = data;
						thisPlayer.handleAccessRestriction(intendedAction);
					}
					else{
						thisPlayer.throwError(new Error(thisPlayer.getLabel('videoAccessError')), true);
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
		var thisPlayer = this;
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
			this.stopLoader();
		}
		else{
			if(!node){
				node = this.configuration.startScene;
			}
			
			if(!this.isCurrentScene(node) && $('.sivaPlayer_annotationEditor', this.player).length != 0 && !confirm(this.getLabel('annotationEditorLeaveMessage'))){
				this.prepareReplay();
			}
			else if(!this.isCurrentScene(node)){
				this.updateHistory(node);
				this.createTopControls(node, true);
				this.setScene(node, startTime, autostart);
			}
			else if(this.currentSceneEnded){
				this.playVideo();
			}
		}
	};
	
	this.setScene = function(scene, startTime, autostart){
		var thisPlayer = this;
		this.sendChromecastMessage('setScene', [scene, startTime, autostart]);
		this.currentSceneEnded = false;
		this.startLoader();
		if(this.configuration.accessRestriction && this.isAccessRestrictionSessionExpired(function(){thisPlayer.setScene(scene, startTime, autostart);})){
			return;
		}
		this.currentScene = scene;
		this.sceneHistory[scene] = true;
		this.logAction('loadScene', scene, this.configuration.scenes[scene].title[this.currentLanguage].content);
		$('.sivaPlayer_bottomControls, .sivaPlayer_annotationSidebar, .sivaPlayer_annotationEditor, .sivaPlayer_annotationSidebarButton, .sivaPlayer_searchSidebar, .sivaPlayer_videoContainer .sivaPlayer_annotation, .sivaPlayer_popup, .sivaPlayer_nodeSelectionSidebar', this.player).remove();
		if($('.sivaPlayer_mainVideo', this.player).length == 0 || this.isMobile){
			$('.sivaPlayer_videoBackground', this.player).remove();
			var videoElement = '<video class="sivaPlayer_mainVideo">';
			var testElement = document.createElement("video");
			for(var i = 0; i < this.configuration.scenes[scene].files.length; i++){
				var file = this.configuration.scenes[scene].files[i];
				if(testElement.canPlayType(file.type) != ''){
					this.configuration.supportedVideoTypes.push(file.type);
					videoElement += '<source src="' + this.appendAccessToken(this.configuration.videoPath + file.url[this.currentLanguage].href + "." + file.format) + '" type="' + file.type + '" />';
				}
			}
			if(this.configuration.supportedVideoTypes.length == 0){;
				this.throwError(new Error(this.getLabel('noSupportedVideoFormatError')), false);
			}
			videoElement += '</video>';
			$(this.player).append('<div class="sivaPlayer_videoBackground"><div class="sivaPlayer_overlayButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 300 300"><circle cx="150" cy="150" r="150"/><g class="sivaPlayer_playButton" title="' + this.getLabel('playTooltip') + '"><polygon points="108.5,82.767 108.5,225.856 232.5,154.31 "/></g><g class="sivaPlayer_replayButton" title="' + this.getLabel('replayTooltip') + '"><path d="M149.539,64.941c-15.144,0-29.366,3.874-41.87,10.596L81.087,53.065l-12.647,95.786l81.024-37.991 l-24.24-20.486c7.577-2.896,15.741-4.557,24.315-4.557c38.209,0,69.193,31.392,69.193,70.128c0,38.73-30.984,70.12-69.193,70.12 c-32.647,0-59.939-22.94-67.226-53.769l-21.645-4.021c5.953,44.404,43.414,78.658,88.87,78.658 c49.592,0,89.792-40.736,89.792-90.989C239.331,105.678,199.131,64.941,149.539,64.941z"/></g></svg></div><div class="sivaPlayer_videoContainer">' + videoElement + '</div></div>')
			.removeClass('sivaPlayer_replay');
			
			$('.sivaPlayer_videoBackground', thisPlayer.player).click(function(){
				var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player);
				var prevPaused = false;
				if(videoElement.length > 0){
					prevPaused = videoElement[0].paused;
				}
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
					$('.sivaPlayer_mainVideo', this.player).attr('src', this.appendAccessToken(this.configuration.videoPath + file.url[this.currentLanguage].href + "." + file.format));
					break;
				}
			}
		}
		
		this.initChromecastReceiver();

		this.getCommunityAnnotations(scene, function(){
			$('.sivaPlayer_mainVideo', this.player).unbind()
			.bind('loadeddata', function(){
				thisPlayer.configuration.ratio = this.videoWidth / this.videoHeight;
				thisPlayer.onLoadedData(scene, startTime, autostart);
			})
			.bind('loadstart', function(){
				if(thisPlayer.isMobile){
					$('.sivaPlayer_topControls', thisPlayer.player).remove();
					var helper = $('<div class="sivaPlayer_mobileHelper"></div>')
					.click(function(){
						thisPlayer.configuration.ratio = this.videoWidth / this.videoHeight;
						thisPlayer.playVideo();
						thisPlayer.onLoadedData(scene, startTime, autostart);
					});
					$(thisPlayer.player).append(helper);
					thisPlayer.stopLoader();
				}
			})
			.bind('play', function(){
				$(thisPlayer.player).addClass('sivaPlayer_playing');
				thisPlayer.tidyPlayer();
				thisPlayer.prepareReplay();
			})
			.bind('pause', function(){
				$(thisPlayer.player).removeClass('sivaPlayer_playing');
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
		});
	};
	
	this.onLoadedData = function(scene, startTime, autostart){
		var thisPlayer = this;
		$('.sivaPlayer_mobileHelper', this.player).remove();
		if(this.active){
			if($('.sivaPlayer_topControls', this.player).length == 0){
				this.createTopControls(scene, true);
			}
			this.setCurrentSceneTime(startTime);
			this.stopLoader();
			var amountSidebarAnnotations = 0;
			var amountSubtitles = 0;
			var amountDisableableAnnotations = 0;
			for(var i = 0; i < this.configuration.globalAnnotations.length; i++){
				var annotation = this.configuration.annotations[(this.configuration.globalAnnotations[i])];
				if(annotation.type == 'subTitle'){
					amountSubtitles++;
				}
				if(annotation.isSidebarAnnotation){
					amountSidebarAnnotations++;
				}
				if(!annotation.isSidebarAnnotation && annotation.disableable){
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
					amountSidebarAnnotations++;
				}
				if(annotation.target && thisPlayer.configuration.annotations[annotation.target].isSidebarAnnotation){
					amountSidebarAnnotations++;
				}
				if(!annotation.isSidebarAnnotation && annotation.disableable){
					amountDisableableAnnotations++;
				}
			}
			this.createControls(amountSubtitles, amountDisableableAnnotations);
			this.createAnnotationSidebar();
			if(amountSidebarAnnotations == 0 && !this.configuration.common.collaboration){
				$('.sivaPlayer_annotationSidebar, .sivaPlayer_annotationSidebarButton', this.player).addClass('sivaPlayer_empty');
			}
			this.setVolume(true, true);
			this.createAnnotations();
			this.setProportions();
			if((amountSidebarAnnotations > 0 || this.configuration.common.collaboration) && (this.isAnnotationSidebarVisible || this.isAnnotationSidebarVisibleUserSelection)){
				this.slideInAnnotationSidebar();
			}
			else{
				this.isAnnotationSidebarVisible = false;
			}
			if(autostart){
				this.playVideo();
			}
			else{
				this.pauseVideo();
			}
			this.setProportions();
		}
	};
	
	this.onSceneEnd = function(videoElement){
		if(this.currentSceneEnded)
			return;
		videoElement.pause();
		$(this.player).addClass('sivaPlayer_replay');
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
	
	this.isCurrentScene = function(scene){
		var currentScene = this.history[(this.history.length - 1)];
		return (currentScene == scene);
	};
	
	this.updateHistory = function(scene){
		var tmp = [];
		for(var i = 0; i < this.history.length && this.history[i] != scene; i++){
			tmp.push(this.history[i]);			
		}
		tmp.push(scene);
		this.history = tmp;
	};
	
	this.onPhonegap = function(){
		var thisPlayer = this;
		this.isMobile = false;
		this.phonegap = true;
		this.onChromecastAvailable();
		window.plugins.insomnia.keepAwake();
		document.addEventListener("backbutton", function(){
			thisPlayer.onBackButton();
		}, true);
	};
	
	this.onBackButton = function(){
		if($('.sivaPlayer_volumeControl, .sivaPlayer_popup:not(.sivaPlayer_nodeSelectionPopup), .sivaPlayer_searchSidebar, .sivaPlayer_annotationEditor', this.player).length > 0){
			this.tidyPlayer(800, true, true);
		}
		else if(this.history.length > 0 && (this.history.length > 1 || this.history[0] != this.currentScene)){
			history.back();
		}
		else{
			navigator.app.exitApp();
		}
	};
	
	this.playVideo = function(){
		this.sendChromecastMessage('playVideo');
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
	};
	
	this.pauseVideo = function(){
		this.sendChromecastMessage('pauseVideo');
		var videoElements = $('.sivaPlayer_mainVideo', this.player);
		if(videoElements.length > 0){
			videoElements[0].pause();
		}
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
	
	this.prepareReplay = function(){
		if($(this.player).hasClass('sivaPlayer_replay')){
			$(this.player).removeClass('sivaPlayer_replay');
			var node = this.history[(this.history.length - 1)];
			var next = {};
			next[this.arrayPosition] = node + '|' + this.hashchangeCounter;
			$.bbq.pushState(next);
		}
	};
	
	this.setVolume = function(isSetByUser, hideVolumeControl){
		var thisPlayer = this;
		this.sendChromecastMessage('setVolume', [this.volume, isSetByUser, hideVolumeControl]);
		$.each($('video, audio', this.player), function(){
			this.volume = (thisPlayer.chromecastSession) ? 0 : thisPlayer.volume;
		});
		if(isSetByUser){
			this.previousVolume = -1;
			sivaPlayerStorage.set('volume', JSON.stringify({
				'percent': this.volume,
				'time': (new Date()).getTime()
			}));
		}
		this.volumeSetByUser = isSetByUser;
		if(hideVolumeControl){
			$('.sivaPlayer_volumeControl', this.player).remove();
		}
	};
	
	this.createControls = function(amountSubtitles, amountDisableableAnnotations){
		var thisPlayer = this;		
		var timelineMouseDown = false;
		var controls = $('<tr></tr>');
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
		var playPauseButton = $('<td class="sivaPlayer_button sivaPlayer_playPauseButton sivaPlayer_noChromecastButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_playButton" title="' + this.getLabel('playTooltip') + '"><polygon points="2,0 2,30 28,15"/></g><g class="sivaPlayer_pauseButton" title="' + this.getLabel('pauseTooltip') + '"><rect x="2" width="10.4" height="30"/><rect x="17.6" width="10.4" height="30"/></g><g class="sivaPlayer_replayButton" title="' + this.getLabel('replayTooltip') + '"><path d="M14.928,1.838c-2.375,0-4.605,0.599-6.566,1.64L4.192,0L2.209,14.822l12.707-5.878l-3.802-3.17 c1.188-0.448,2.469-0.706,3.813-0.706c5.992,0,10.852,4.857,10.852,10.852c0,5.994-4.859,10.851-10.852,10.851 c-5.12,0-9.401-3.55-10.542-8.32l-3.395-0.622C1.924,24.699,7.799,30,14.928,30c7.777,0,14.082-6.304,14.082-14.081 C29.01,8.142,22.705,1.838,14.928,1.838z"/></g></svg></td>')
		.click(function(){
			var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player);
			var prevPaused = false;
			if(videoElement.length > 0){
				prevPaused = videoElement[0].paused;
			}
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
			if(timelineMouseDown){
				thisPlayer.changeTimeline(this, e);
			}
		})
		.bind('mouseleave', function(){
			$('.sivaPlayer_timelineUpdate, .sivaPlayer_timlineUpdateSelectedTime', this).remove();
		})
		.mousedown(function(e){
			thisPlayer.changeTimeline(this, e);
			timelineMouseDown = true;
		})
		.mouseup(function(){
			timelineMouseDown = false;
			$('.sivaPlayer_timelineUpdate, .sivaPlayer_timlineUpdateSelectedTime', this).remove();
		});
		controls.append('<td class="sivaPlayer_spacer"></td>');
		var sound = $('<td class="sivaPlayer_button sivaPlayer_volumeButton sivaPlayer_noChromecastButton"><svg title="' + this.getLabel('volumeTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_muteButton"><path d="M26.533,2.611L0,26.595l1.427,1.712L27.961,4.322L26.533,2.611z M18.289,15.655c-0.045,6.851-0.784,12.307-1.695,12.307c-0.75,0-1.777-3.695-2.161-8.811l-4.978,4.494 c2.116,3.11,4.46,6.354,5.05,6.354h2.472c1.634,0,2.861-6.75,2.861-15.033l-0.006-0.705L18.289,15.655z M4.294,20.623h0.195 l10.383-9.378c-0.139-0.134-0.289-0.25-0.445-0.345c0.384-5.15,1.412-8.861,2.167-8.861c0.623,0,1.162,2.539,1.462,6.328 l1.389-1.256C18.972,2.839,18.105,0,17.044,0h-2.539c-0.8,0-4.395,5.64-6.694,9.378H4.294c-0.895,0-1.623,2.495-1.623,5.623 C2.672,18.128,3.4,20.623,4.294,20.623z"/></g><g class="sivaPlayer_unmuteButton"><path d="M26.881,4.771L25.27,5.718c1.268,2.919,1.939,6.145,1.939,9.357 s-0.672,6.438-1.939,9.357l1.611,0.947c1.377-3.221,2.1-6.778,2.1-10.305C28.98,11.543,28.258,7.99,26.881,4.771z M24.525,9.226 l-1.717,0.839c0.635,1.587,1.127,3.309,1.127,5.03s-0.492,3.469-1.127,5.056l1.639,0.569c0.762-1.875,1.268-3.692,1.268-5.708 C25.715,13.033,25.273,11.088,24.525,9.226z M20.527,13.424c0.494,1.07,0.486,2.454-0.021,3.796l1.496,0.565 c0.658-1.741,0.65-3.574-0.02-5.03L20.527,13.424z M16.999,0H14.45c-0.799,0-4.408,5.638-6.713,9.375H6.108h-1.51h-0.39 c-0.894,0-1.623,2.497-1.623,5.625c0,3.129,0.729,5.625,1.623,5.625h1.899v0.021h1.268C9.694,24.16,13.664,30,14.45,30h2.479 c1.639,0,2.871-6.75,2.871-15.032S18.633,0,16.999,0z M16.547,27.96c-0.756,0-1.786-3.716-2.173-8.858 c1.052-0.644,1.797-2.235,1.797-4.102c0-1.871-0.745-3.458-1.797-4.102c0.387-5.147,1.417-8.857,2.173-8.857 c0.943,0,1.705,5.801,1.705,12.959C18.252,22.152,17.49,27.96,16.547,27.96z"/></g></svg></td>');
		$('svg', sound).click(function(){
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
			controls.append('<td class="sivaPlayer_spacer sivaPlayer_noChromecastButton"></td>');
			var subTitleButton = $('<td class="sivaPlayer_button sivaPlayer_noChromecastButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_subtitleOnButton" title="' + this.getLabel('subtitleOnTooltip') + '"><path d="M0,3v23h30V3H0z M5,17h20v2H5V17z M28,23H2v-2h26V23z"/></g><g class="sivaPlayer_subtitleOffButton" title="' + this.getLabel('subtitleOffTooltip') + '"><g><polygon points="16.306,17 25,17 25,19 14.093,19 11.88,21 28,21 28,23 9.667,23 6.349,26 29,26 29,5.621"/><polygon points="2,22.227 2,21 3.357,21 5.57,19 5,19 5,17 7.783,17 23.271,3 1,3 1,23.035"/></g><path d="M27.553,1.486l1.428,1.711L2.447,27.182L1.02,25.47L27.553,1.486z"/></g></svg></td>')
			.click(function(){
				thisPlayer.logAction('useButton', 'subTitle', ((thisPlayer.isSubTitleVisible) ? 'hide' : 'show'));
				thisPlayer.isSubTitleVisible = !thisPlayer.isSubTitleVisible;
				thisPlayer.setSubtitleButton();
				thisPlayer.updateAnnotations();
			});
			controls.append(subTitleButton);
		}
		if(amountDisableableAnnotations > 0){
			controls.append('<td class="sivaPlayer_spacer sivaPlayer_noChromecastButton"></td>');
			var annotationButton = $('<td class="sivaPlayer_button"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_annotationOnButton" title="' + this.getLabel('annotationOnTooltip') + '"><g><path d="M1,3v23h28V3H1z M28,25H2V4h26V25z"/><g><rect x="4" y="6" width="16" height="4"/></g><g><rect x="17" y="12" width="10" height="10"/></g><ellipse cx="8.75" cy="17.416" rx="4.448" ry="3.813"/></g></g><g class="sivaPlayer_annotationOffButton" title="' + this.getLabel('annotationOffTooltip') + '"><path d="M27.395,2.279l1.428,1.711L2.29,27.975l-1.428-1.712L27.395,2.279z"/><g><polygon points="2,22.768 2,4 22.683,4 23.785,3 1,3 1,23.675"/><polygon points="28,6.973 28,25 8.027,25 6.919,26 29,26 29,6.07"/></g><path d="M11.492,14.437c-0.758-0.513-1.702-0.832-2.742-0.832c-2.456,0-4.448,1.707-4.448,3.812c0,0.97,0.436,1.845,1.132,2.518L11.492,14.437z"/><g><polygon points="20,6.435 20,6 4,6 4,10 16.621,10"/></g><g><polygon points="17,16.784 17,22 27,22 27,12 22.688,12"/></g></g></svg></td>')
			.click(function(){
				thisPlayer.logAction('useButton', 'annotation', ((thisPlayer.isSubTitleVisible) ? 'hide' : 'show'));
				thisPlayer.isAnnotationVisible = !thisPlayer.isAnnotationVisible;
				thisPlayer.setAnnotationButton();
				thisPlayer.updateAnnotations();
			});
			controls.append(annotationButton);
		}
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
		var settings = $('<td class="sivaPlayer_button sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"><svg title="' + this.getLabel('settingsTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><path d="M28.97,20.526L30,15.831l-4.81-2.11l-0.02,0.09c-0.038-0.321-0.088-0.642-0.157-0.964c-0.034-0.156-0.083-0.305-0.124-0.458 l0.193,0.301l3.782-3.644l-2.591-4.049l-4.893,1.91l0.12,0.188c-0.375-0.309-0.772-0.587-1.186-0.839l0.112,0.024l0.097-5.251 L15.83,0l-2.109,4.811l0.09,0.02c-0.321,0.038-0.642,0.088-0.964,0.157c-0.157,0.034-0.306,0.083-0.459,0.124l0.303-0.194 L9.047,1.133L4.998,3.726l1.909,4.893l0.189-0.121C6.788,8.873,6.51,9.27,6.257,9.684l0.024-0.111L1.031,9.475L0,14.17l4.811,2.109 l0.02-0.09c0.038,0.321,0.088,0.642,0.157,0.964c0.017,0.076,0.042,0.147,0.06,0.222l-3.751,3.613l2.592,4.048l4.892-1.908 c0.291,0.222,0.593,0.425,0.905,0.615l-0.112-0.024L9.474,28.97L14.169,30l2.11-4.81l-0.091-0.02 c0.321-0.038,0.643-0.087,0.965-0.157c0.075-0.017,0.146-0.042,0.221-0.06l3.613,3.751l4.049-2.591l-1.909-4.892 c0.222-0.29,0.426-0.593,0.616-0.906l-0.024,0.112L28.97,20.526z M16.039,19.832c-2.668,0.575-5.296-1.124-5.871-3.793 c-0.573-2.667,1.125-5.296,3.793-5.869c2.669-0.574,5.296,1.124,5.871,3.792C20.405,16.629,18.707,19.258,16.039,19.832z"/></svg></td>')
		.click(function(){
			thisPlayer.logAction('useButton', 'settings', '');
			thisPlayer.setPlayedBefore();
			thisPlayer.createSettingsPopup();
		});
		controls.append(settings);
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
		$(this.player).append($('<table class="sivaPlayer_bottomControls sivaPlayer_controls sivaPlayer_transition"></table').append(controls));
		this.setSubtitleButton();
		this.setAnnotationButton();
		this.updateTimeline(true);
		
		$('.sivaPlayer_controls', this.player).hover(function(){
			$(thisPlayer.player).addClass('sivaPlayer_controlsHover');
		}, function(){
			$(thisPlayer.player).removeClass('sivaPlayer_controlsHover');
		});
	};
	
	this.createTopControls = function(node, isScene){
		var thisPlayer = this;
		if(!this.configuration.scenes[node] && isScene){
			this.throwError(new Error(this.getLabel('unknownSceneOrNode')), false);
		}	
		$('.sivaPlayer_topControls', this.player).remove();
		var controls = $('<tr></tr>');
		var mobileControls = $('<tr></tr>');
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
		controls.append('<td class="sivaPlayer_button sivaPlayer_tableOfContentsButton sivaPlayer_noChromecastButton"><svg title="' + this.getLabel('tableOfContentsTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_noMobileButton"><rect y="1" width="29" height="4"/><rect y="7" width="25" height="4"/><rect y="13" width="29" height="4"/><rect y="25" width="4" height="4"/><rect x="6" y="25" width="4" height="4"/><rect x="12" y="25" width="4" height="4"/><rect y="19" width="28" height="4"/></g><g class="sivaPlayer_mobileButton"><rect x="4" y="21" width="22" height="3"/><rect x="4" y="14" width="22" height="3"/><rect x="4" y="7" width="22" height="3"/></g></svg></td>');
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
		controls.append('<td class="sivaPlayer_videoTitle sivaPlayer_noChromecastButton">' + ((this.configuration.tableOfContents && this.configuration.tableOfContents.title[this.currentLanguage].content) ? this.configuration.tableOfContents.title[this.currentLanguage].content : this.configuration.videoTitle[this.currentLanguage].content) + '</td>');
		$('.sivaPlayer_tableOfContentsButton, .sivaPlayer_videoTitle', controls).click(function(){
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
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
		var prevButton = $('<td class="sivaPlayer_prevButton sivaPlayer_button sivaPlayer_noMobileButton sivaPlayer_noChromecastButton ' + ((isScene) ? '' : 'sivaPlayer_disabled') + '"><svg title="' + this.getLabel('backTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><polygon points="30,0 30,30 4,15 "/><rect width="4" height="30"/></svg></td>')
		.click(function(){
			if((thisPlayer.history.length > 1 || thisPlayer.currentSceneTime > 5) && !$(this).hasClass('sivaPlayer_disabled')){
				thisPlayer.pauseVideo();
				thisPlayer.logAction('useButton', 'back', '');
				var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player);
				if(videoElement.length > 0 && videoElement[0].currentTime > 5){
					thisPlayer.sendChromecastMessage('setCurrentSceneTime', [0, false]);
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
		mobileControls.append($(prevButton).clone(true));
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton"></td>');
		mobileControls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton"></td>');
		var title = '<td class="sivaPlayer_sceneTitle sivaPlayer_noMobileButton"><span>' + ((isScene) ? this.configuration.scenes[node].title[this.currentLanguage].content : '') + '</span></td>';
		controls.append(title);
		mobileControls.append(title);
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton"></td>');
		mobileControls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton"></td>');
		var nextButton = $('<td class="sivaPlayer_nextButton sivaPlayer_button sivaPlayer_noMobileButton sivaPlayer_noChromecastButton ' + ((isScene) ? '' : 'sivaPlayer_disabled') + '"><svg title="' + this.getLabel('forwardTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><polygon points="0,0 0,30 26,15 "/><rect x="26" width="4" height="30"/></svg></td>')
		.click(function(){
			if(isScene && !$(this).hasClass('sivaPlayer_disabled')){	
				thisPlayer.logAction('useButton', 'next', '');
				var videoElement = $('.sivaPlayer_mainVideo', thisPlayer.player)[0];
				thisPlayer.playVideo();
				thisPlayer.sendChromecastMessage('setCurrentSceneTime', [videoElement.duration - 0.5]);
				thisPlayer.setCurrentSceneTime(videoElement.duration - 0.5);
				thisPlayer.sendChromecastMessage('updateTimeline', [true]);
				thisPlayer.updateTimeline(true);
			}
		});
		controls.append(nextButton);
		mobileControls.append($(nextButton).clone(true));
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
		controls.append('<td class="sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
		var searchButton = $('<td class="sivaPlayer_button sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"><svg title="' + this.getLabel('searchTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_searchOffButton"><path d="M28.431,1.458l1.53,1.833L1.53,28.991L0,27.157L28.431,1.458z"/><g><path d="M23.986,19.654c-0.498-0.497-1.397-0.449-2.303,0.021l-2.466-2.468c0.802-1.047,1.385-2.208,1.749-3.421l-8.224,7.431 c1.588-0.308,3.121-0.972,4.466-2.001l2.467,2.468c-0.471,0.903-0.518,1.804-0.021,2.302l5.657,5.655 c0.714,0.716,2.263,0.323,3.456-0.871c1.197-1.194,1.59-2.745,0.876-3.46L23.986,19.654z"/><path d="M5.532,20.089l1.792-1.632c-0.941-0.411-1.825-0.994-2.595-1.764c-3.303-3.306-3.306-8.659,0-11.963 c3.303-3.303,8.66-3.303,11.966,0c0.969,0.968,1.645,2.116,2.046,3.334l1.791-1.631c-0.521-1.196-1.267-2.318-2.245-3.297 c-4.184-4.182-10.966-4.182-15.15,0c-4.183,4.185-4.183,10.966,0,15.15C3.865,19.015,4.673,19.614,5.532,20.089z"/></g></g><g class="sivaPlayer_searchOnButton"><path d="M28.768,28.771c1.197-1.194,1.59-2.745,0.876-3.46l-5.657-5.656c-0.498-0.497-1.397-0.449-2.303,0.021 l-2.466-2.468c3.216-4.199,2.912-10.228-0.931-14.072c-4.184-4.182-10.966-4.182-15.15,0c-4.183,4.185-4.183,10.966,0,15.15 c3.843,3.842,9.872,4.146,14.072,0.931l2.467,2.468c-0.471,0.903-0.518,1.804-0.021,2.302l5.657,5.655 C26.025,30.357,27.574,29.965,28.768,28.771z M16.695,16.693c-3.308,3.303-8.663,3.303-11.966,0c-3.303-3.306-3.306-8.659,0-11.963 c3.303-3.303,8.66-3.303,11.966,0C19.998,8.032,19.998,13.387,16.695,16.693z" /></g></svg></td>')
		.click(function(){
			var sidebar = $('.sivaPlayer_searchSidebar', thisPlayer.player);
			if(sidebar.length == 0){
				thisPlayer.logAction('openSearchArea', '', '');
				thisPlayer.createSearchSidebar();
				thisPlayer.setSearchButton();
			}
			else{
				$(sidebar).fadeOut(600, function(){
					$(this).remove();
					thisPlayer.setSearchButton();
				});
				thisPlayer.logAction('closeSearchArea', '', '');
			}
		});
		controls.append(searchButton);
		if(this.configuration.common && this.configuration.common.userDiary && this.configuration.common.log && (this.configuration.accessRestriction || this.configuration.common.logPath)){
			controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
			var stats = $('<td class="sivaPlayer_button sivaPlayer_statsButton sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30"><path d="M0,1.73V29h30V1.73H0z M28.269,25.923H1.731V9.115h26.538V25.923z"/><g><line fill="none" stroke-miterlimit="10" x1="4.125" y1="12.625" x2="25.874" y2="12.625"/><line fill="none" stroke-miterlimit="10" x1="4.125" y1="17.375" x2="25.875" y2="17.375"/><line fill="none" stroke-miterlimit="10" x1="4.125" y1="22.125" x2="25.875" y2="22.125"/><line fill="none" stroke-miterlimit="10" x1="17.875" y1="9.875" x2="17.875" y2="25.124"/><line fill="none" stroke-miterlimit="10" x1="12.875" y1="9.875" x2="12.875" y2="25.125"/><line fill="none" stroke-miterlimit="10" x1="7.875" y1="9.875" x2="7.875" y2="25.125"/><line fill="none" stroke-miterlimit="10" x1="22.875" y1="9.875" x2="22.875" y2="25.124"/></g></svg></td>')
			.click(function(){
				thisPlayer.startLoader();
				thisPlayer.logAction('useButton', 'stats', '');
				thisPlayer.setPlayedBefore();
				thisPlayer.logAction('openStats', '', '');
				thisPlayer.createPopup('stats', true);
				$('.sivaPlayer_statsPopup .sivaPlayer_closeButton', this.player).click(function(){
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
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_chromecast sivaPlayer_noChromecastButton"></td>');
		var chromecastButton = $('<td class="sivaPlayer_button sivaPlayer_chromecast sivaPlayer_noChromecastButton sivaPlayer_transition"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 24 24" xml:space="preserve"><g class="sivaPLayer_chromecastOff"><path d="M1,18 L1,21 L4,21 C4,19.34 2.66,18 1,18 L1,18 Z M1,14 L1,16 C3.76,16 6,18.24 6,21 L8,21 C8,17.13 4.87,14 1,14 L1,14 Z M1,10 L1,12 C5.97,12 10,16.03 10,21 L12,21 C12,14.92 7.07,10 1,10 L1,10 Z M21,3 L3,3 C1.9,3 1,3.9 1,5 L1,8 L3,8 L3,5 L21,5 L21,19 L14,19 L14,21 L21,21 C22.1,21 23,20.1 23,19 L23,5 C23,3.9 22.1,3 21,3 L21,3 Z"></path></g><g class="sivaPlayer_chromecastOn"><path d="M1,18 L1,21 L4,21 C4,19.34 2.66,18 1,18 L1,18 Z M1,14 L1,16 C3.76,16 6,18.24 6,21 L8,21 C8,17.13 4.87,14 1,14 L1,14 Z M19,7 L5,7 L5,8.63 C8.96,9.91 12.09,13.04 13.37,17 L19,17 L19,7 L19,7 Z M1,10 L1,12 C5.97,12 10,16.03 10,21 L12,21 C12,14.92 7.07,10 1,10 L1,10 Z M21,3 L3,3 C1.9,3 1,3.9 1,5 L1,8 L3,8 L3,5 L21,5 L21,19 L14,19 L14,21 L21,21 C22.1,21 23,20.1 23,19 L23,5 C23,3.9 22.1,3 21,3 L21,3 Z"></path></g></svg></td>')
		.click(function(){
			thisPlayer.setPlayedBefore();
			if(thisPlayer.chromecastSession){
				thisPlayer.chromecastSession.stop(function(){
					thisPlayer.chromecastSession = undefined;
					thisPlayer.setVolume(true, true);
					$('.sivaPlayer_chromecastOn', thisPlayer.player).hide(0);
				}, function(e){
					alert('Ein Fehler ist aufgetreten.');
					thisPlayer.restorePlayedBefore();
				});
			}
			else{
				var sessionRequest = new chrome.cast.SessionRequest('F2CC2A3A');
				var apiConfig = new chrome.cast.ApiConfig(sessionRequest, function (e){
					session = e;
					if(session.media.length != 0){
						thisPlayer.onChromecastMediaDiscovered('onRequestSessionSuccess', session.media[0]);
					}				
				}, function(e){
					if(e !== chrome.cast.ReceiverAvailability.AVAILABLE) {
						thisPlayer.restorePlayedBefore();
					}				
				});
				chrome.cast.initialize(apiConfig, function(){
					console.log('init');
					chrome.cast.requestSession(function(e){
						thisPlayer.chromecastSession = e;
						console.log(thisPlayer.chromecastSession);
						$('.sivaPlayer_chromecastOn', thisPlayer.player).show(0);
						thisPlayer.sendChromecastMessage('setScene', [node, thisPlayer.currentSceneTime, false]);
						thisPlayer.setVolume(true, true);
						thisPlayer.tidyPlayer(0, true, false);
						thisPlayer.restorePlayedBefore();
					}, function(e){
						console.log(2, e);
						thisPlayer.restorePlayedBefore();
					});
				}, function(e){
					console.log(1, e);
					alert('Ein Fehler ist aufgetreten.');
					thisPlayer.restorePlayedBefore();
				});
			}
		});
		controls.append(chromecastButton);
		if(Modernizr.fullscreen){
			controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
			var fullscreenButton = $('<td class="sivaPlayer_button sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_buttonNormalScreen" title="' + this.getLabel('closeFullscreenTooltip') + '"><polygon points="16.863,13.127 18.451,2.821 21.17,5.542 26.721,-0.008 29.972,3.245 24.422,8.794 27.171,11.541 "/><polygon points="13.138,16.873 11.55,27.178 8.831,24.457 3.28,30.008 0.027,26.756 5.577,21.206 2.831,18.459"/><polygon points="13.136,13.103 2.83,11.514 5.549,8.793 0,3.243 3.252,-0.008 8.802,5.542 11.549,2.794"/><polygon points="16.863,16.898 27.171,18.486 24.45,21.206 30,26.756 26.747,30.008 21.197,24.457 18.451,27.204"/></g><g class="sivaPlayer_buttonFullScreen" title="' + this.getLabel('fullscreenTooltip') + '"><polygon points="30,0.001 28.412,10.302 25.695,7.583 20.148,13.131 16.896,9.878 22.443,4.333 19.698,1.586"/><polygon points="0,29.999 1.588,19.699 4.306,22.418 9.852,16.871 13.104,20.121 7.557,25.668 10.302,28.415"/><polygon points="0,0 10.302,1.586 7.584,4.307 13.13,9.854 9.879,13.104 4.331,7.557 1.587,10.302"/><polygon points="30,30 19.698,28.414 22.417,25.693 16.871,20.147 20.121,16.898 25.669,22.444 28.414,19.699"/></g></svg></td>')
			.click(function(){
				thisPlayer.changeFullscreenMode();
			});
			controls.append(fullscreenButton);
		}
		controls.append('<td class="sivaPlayer_spacer sivaPlayer_noMobileButton sivaPlayer_noChromecastButton"></td>');
		$(this.player).append($('<table class="sivaPlayer_controls sivaPlayer_topControls sivaPlayer_transition"></table>').append(controls));
		$(this.player).append($('<table class="sivaPlayer_controls sivaPlayer_topControls sivaPlayer_mobileControls sivaPlayer_transition"></table>').append(mobileControls));
		if(this.history.length <= 1){
			$('.sivaPlayer_prevButton', this.player).addClass('sivaPlayer_disabled');
		}
		this.setFullscreenButton();
		if(this.isChromecastAvailable){
			$('.sivaPlayer_chromecast', this.player).show(0);
			if(this.chromecastSession){
				$('.sivaPlayer_chromecastOn', this.player).show(0);
			}
		}
	};
	
	this.changeTimeline = function(timeline, event){
		this.prepareReplay();
		var videoElement = $('.sivaPlayer_mainVideo', this.player)[0];
		if(videoElement.paused){
			this.prepareReplay();
		}
		var percentage = (event.pageX - $(timeline).offset().left) / $(timeline).width();
		var duration = videoElement.duration;
		this.logAction('selectTime', '', (duration * percentage) + '');
		this.currentSceneEnded = false;
		$('.sivaPlayer_topControls .sivaPlayer_nextButton', this.player).removeClass('sivaPlayer_disabled');
		this.sendChromecastMessage('setCurrentSceneTime', [duration * percentage]);
		this.setCurrentSceneTime(duration * percentage);
		this.sendChromecastMessage('updateTimeline', [true]);
		this.updateTimeline(true);
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
			$('.sivaPlayer_searchOnButton', this.player).show(0);
			$('.sivaPlayer_searchOffButton', this.player).hide(0);
		}
		else{
			$('.sivaPlayer_searchOnButton', this.player).hide(0);
			$('.sivaPlayer_searchOffButton', this.player).show(0);
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
			this.logAction('openFullscreen', '', '');
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
			this.logAction('closeFullscreen', '', '');
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
	
	this.onChromecastAvailable = function(){
		this.isChromecastAvailable = true;
		$('.sivaPlayer_chromecast', this.player).show(0);
	};
	
	this.initChromecastReceiver = function(){
		var thisPlayer = this;
		if(!this.configuration.common.isChromecastReceiver || this.chromecastMessageBus){
			return;
		}
		var mediaManager = new cast.receiver.MediaManager($('.sivaPlayer_mainVideo', this.player)[0]);
		var castReceiverManager = cast.receiver.CastReceiverManager.getInstance();
		this.chromecastMessageBus = castReceiverManager.getCastMessageBus('urn:x-cast:de.uni-passau.mirkul', cast.receiver.CastMessageBus.MessageType.JSON);
		this.chromecastMessageBus.onMessage = function(event){
			var method = event.data.method;
			var parameters = event.data.parameters;
			console.log(method, parameters);
			switch(method){
			case 'setScene':
				thisPlayer.createTopControls(parameters[0], true);
				thisPlayer.setScene(parameters[0], parameters[1], parameters[2]);
				break;
			case 'playVideo':
				thisPlayer.playVideo();
				break;
			case 'pauseVideo':
				thisPlayer.pauseVideo();
				break;
			case 'setCurrentSceneTime':
				thisPlayer.setCurrentSceneTime(parameters[0]);
				break;
			case 'updateTimeline':
				thisPlayer.updateTimeline(parameters[0]);
				break;
			case 'setVolume':
				thisPlayer.volume = parameters[0];
				thisPlayer.setVolume(parameters[1], parameters[2]);
				break;
			case 'createImageZoom':
				thisPlayer.createImageZoom(parameters[0], parameters[1]);
				break;
			case 'setZoomImage':
				thisPlayer.setZoomImage(parameters[0]);
				break;
			case 'createPdfZoom':
				thisPlayer.createPdfZoom(parameters[0]);
				break;
			case 'createRichtextZoom':
				thisPlayer.createRichtextZoom(parameters[0]);
				break;
			case 'createCommunityZoom':
				thisPlayer.createCommunityZoom(parameters[0]);
				break;
			case 'createMediaZoom':
				thisPlayer.createMediaZoom(parameters[0], false);
				break;
			case 'closePopups':
				thisPlayer.closePopups(parameters[0]);
				break;
			}
		};
		castReceiverManager.onSenderDisconnected = function(){
			global.close();
		};
		castReceiverManager.start();
	};
	
	this.sendChromecastMessage = function(method, parameters){
		var message = {'method': method, 'parameters': parameters};
		if(this.isChromecastAvailable && this.chromecastSession){
			this.chromecastSession.sendMessage('urn:x-cast:de.uni-passau.mirkul', message, function(){}, function(e){
				console.log(e);
			});
		}
		else if(this.configuration.common.isChromecastReceiver && this.chromecastMessageBus){
			this.chromecastMessageBus.send(message);
		}
	};
	
	this.onUnload = function(){
		this.logAction('leaveVideo', '', '');
		if(this.chromecastSession){
			this.chromecastSession.stop(function(){
				thisPlayer.chromecastSession = undefined;
				thisPlayer.setVolume(true, true);
				$('.sivaPlayer_chromecastOn', thisPlayer.player).hide(0);
			}, function(e){
				alert('Ein Fehler ist aufgetreten.');
			});
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
			'url': this.appendAccessToken(((this.configuration.common.logPath) ? this.configuration.common.logPath : this.configuration.videoPath) + '/getStats.js', true, true),
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
		var thisPlayer = this;
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
		if(this.configuration.common.isChromecastReceiver){
			return;
		}
		var sidebar = $('<div class="sivaPlayer_annotationSidebar"></div>')
		.css({'width': parseInt(this.configuration.style.annotationSidebarWidth * 100) + '%', 'right': '-' + parseInt(this.configuration.style.annotationSidebarWidth * 100) + '%'});
		if(this.configuration.common.collaboration){
			$(sidebar).append('<div class="sivaPlayer_sidebarTabs"><span class="sivaPlayer_authorAnnotationTab">' + this.getLabel('authorAnnotationTab') + '</span><span class="sivaPlayer_allAnnotationTab">' + this.getLabel('allAnnotationTab') + '</span><span class="sivaPlayer_communityAnnotationTab">' + this.getLabel('communityAnnotationTab') + '</span></div>')
			.append('<div class="sivaPlayer_scrollHolder"><div class="sivaPlayer_authorAnnotations sivaPlayer_sidebarContents"></div><div class="sivaPlayer_allAnnotations sivaPlayer_sidebarContents"></div><div class="sivaPlayer_communityAnnotations sivaPlayer_sidebarContents"></div></div>');
			$('.sivaPlayer_sidebarContents', sidebar).append('<div class="sivaPlayer_newCommunityAnnotation sivaPlayer_noAnnotation"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="11px" height="11px" viewBox="0 0 11 11"><rect y="4" width="11" height="3" /><rect x="4" width="3" height="11" /></svg> ' + this.getLabel('communityCreateNewAnnotation') + '</div>');
		}
		else{
			$(sidebar).append('<div class="sivaPlayer_scrollHolder"><div class="sivaPlayer_authorAnnotations sivaPlayer_sidebarContents"></div></div>');
			$('.sivaPlayer_sidebarContents', sidebar).append('<div class="sivaPlayer_title sivaPlayer_noAnnotation">' + this.getLabel('additionalInformation') + '</div>');
			this.visibleSidebarAnnotationType = 'author';
		}
		$('.sivaPlayer_sidebarTabs span', sidebar).removeClass('sivaPlayer_active');
		if(this.visibleSidebarAnnotationType == 'author'){
			$('.sivaPlayer_authorAnnotationTab, .sivaPlayer_authorAnnotations', sidebar).addClass('sivaPlayer_active');
		}
		else if(this.visibleSidebarAnnotationType == 'community'){
			$('.sivaPlayer_communityAnnotationTab, .sivaPlayer_communityAnnotations', sidebar).addClass('sivaPlayer_active');
		}
		else{
			$('.sivaPlayer_allAnnotationTab, .sivaPlayer_allAnnotations', sidebar).addClass('sivaPlayer_active');
		}
		$('.sivaPlayer_sidebarTabs span', sidebar).click(function(){
			var tab = $(this).attr('class').replace(/sivaPlayer_hover/, '').replace(/sivaPlayer_/, '').replace(/AnnotationTab/, '').trim();
			thisPlayer.logAction('changeAnnotationAreaTab', tab, '');
			thisPlayer.visibleSidebarAnnotationType = tab;
			sivaPlayerStorage.set('visibleSidebarAnnotationType', tab);
			$('.sivaPlayer_sidebarTabs span', sidebar).removeClass('sivaPlayer_active');
			$(this, sidebar).addClass('sivaPlayer_active');
			$('.sivaPlayer_sidebarContents', sidebar).removeClass('sivaPlayer_active');
			$('.sivaPlayer_sidebarContents.sivaPlayer_' + tab + 'Annotations', sidebar).addClass('sivaPlayer_active');
			thisPlayer.updateAnnotations();
		})
		.hover(function(){
			$(this).addClass('sivaPlayer_hover');
		}, function(){
			$(this).removeClass('sivaPlayer_hover');
		});
		$('.sivaPlayer_newCommunityAnnotation', sidebar).click(function(){
			thisPlayer.logAction('openAnnotationEditor', '', '');
			thisPlayer.createAnnotationEditor();
		});
		if(!this.configuration.common.annotationSidebarVisibility || (this.configuration.common.annotationSidebarVisibility != 'always' && this.configuration.common.annotationSidebarVisibility != 'never')){
			var annotationSidebarButton = $('<div class="sivaPlayer_annotationSidebarButton"><span class="sivaPlayer_button"><svg title="' + this.getLabel('openAnnotationSidebarTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_annotationSidebarOpenButton"><polygon points="27.536,0 12.5,15 27.536,30 28.9,28.5 15.4,15 28.9,1.5 "/><polygon points="16.537,0 1.5,15 16.537,30 17.9,28.5 4.4,15 17.9,1.5 "/></g><g class="sivaPlayer_annotationSidebarCloseButton"><polygon points="2.864,0 17.9,15 2.864,30 1.5,28.5 15,15 1.5,1.5 "/><polygon points="13.863,0 28.9,15 13.863,30 12.5,28.5 26,15 12.5,1.5 "/></g></svg></span></div>')
			.click(function(){
				if(!thisPlayer.isAnnotationSidebarVisible){
					thisPlayer.logAction('openAnnotationArea', '', '');
					thisPlayer.isAnnotationSidebarVisibleUserSelection = true;
					thisPlayer.slideInAnnotationSidebar();
				}
				else{
					thisPlayer.logAction('closeAnnotationArea', '', '');
					thisPlayer.isAnnotationSidebarVisibleUserSelection = false;
					thisPlayer.slideOutAnnotationSidebar();
				}
			});
			$(this.player).append(annotationSidebarButton);
		}
		$(this.player).append(sidebar);
		$('.sivaPlayer_annotationSidebar, .sivaPlayer_annotationSidebarButton', this.player)
		.bind('swipeleft', function(e){
			if(!$(this.player).is('.sivaPlayer_portrait, .sivaPlayer_landscape')){
				thisPlayer.logAction('openAnnotationArea', '', '');
				thisPlayer.isAnnotationSidebarVisibleUserSelection = true;
				thisPlayer.slideInAnnotationSidebar();
				e.stopPropagation();
			}
		})
		.bind('swiperight', function(e){
			if(!$(this.player).is('.sivaPlayer_portrait, .sivaPlayer_landscape')){
				thisPlayer.logAction('closeAnnotationArea', '', '');
				thisPlayer.isAnnotationSidebarVisibleUserSelection = false;
				thisPlayer.slideOutAnnotationSidebar();
				e.stopPropagation();
			}
		});
	};
	
	this.slideInAnnotationSidebar = function(){
		var thisPlayer = this;
		if($('.sivaPlayer_annotationSidebar').hasClass('sivaPlayer_update') || this.mode != 'default'){
			return;
		}
		if(!this.configuration.common.annotationSidebarVisibility || this.configuration.common.annotationSidebarVisibility != 'never'){
			this.isAnnotationSidebarVisible = true;
			$('.sivaPlayer_volumeControl', this.player).remove();
			$('.sivaPlayer_annotationSidebar', this.player).addClass('sivaPlayer_update');
			$('.sivaPlayer_videoContainer', this.player).css(this.getVideoContainerProportions($('.sivaPlayer_mainVideo', this.player), $(this.player).width(), $(this.player).height()));
			$('.sivaPlayer_annotationSidebarButton', this.player).addClass('sivaPlayer_open').css({'right': parseInt(this.configuration.style.annotationSidebarWidth * 100) + '%'});
			$('.sivaPlayer_annotationSidebar', this.player).css({'right': '0'});
			this.setProportions();
			setTimeout(function(){
				$('.sivaPlayer_annotationSidebar', thisPlayer.player).removeClass('sivaPlayer_update');
			}, 500);
		}
	};
	
	this.slideOutAnnotationSidebar = function(){
		var thisPlayer = this;
		if($('.sivaPlayer_annotationSidebar').hasClass('sivaPlayer_update') || this.mode != 'default'){
			return;
		}
		if(!this.configuration.common.annotationSidebarVisibility || this.configuration.common.annotationSidebarVisibility != 'always'){
			this.isAnnotationSidebarVisible = false;
			$('.sivaPlayer_annotationSidebar', this.player).addClass('sivaPlayer_update');
			$('.sivaPlayer_volumeControl', this.player).remove();
			$.each($('.sivaPlayer_annotationSidebar video', this.player), function(){
				this.pause();
			});
			$('.sivaPlayer_videoContainer', this.player).css(this.getVideoContainerProportions($('.sivaPlayer_mainVideo', this.player), $(this.player).width(), $(this.player).height()));
			$('.sivaPlayer_annotationSidebarButton', this.player).removeClass('sivaPlayer_open').css({'right': 0});
			$('.sivaPlayer_annotationSidebar', this.player).css({'right': '-' + parseInt(this.configuration.style.annotationSidebarWidth * 100) + '%'});
			this.setProportions();
			setTimeout(function(){
				$('.sivaPlayer_annotationSidebar', thisPlayer.player).removeClass('sivaPlayer_update');
			}, 500);
		}
	};
	
	this.createAnnotationEditor = function(){
		var thisPlayer = this;
		var videoElement = $('.sivaPlayer_videoContainer video')[0];
		var editor = $('<form class="sivaPlayer_annotationEditor" enctype="multipart/form-data"><div class="sivaPlayer_holder"><div class="sivaPlayer_title">' + this.getLabel('annotationEditorTitle') + '</div></div></form>');
		$('.sivaPlayer_holder', editor).append('<div class="sivaPlayer_input"><input type="text" name="title" class="sivaPlayer_titleField" value="" placeholder="' + this.getLabel('communityTitle') + '" /></div><div class="sivaPlayer_textarea"><textarea name="post" class="sivaPlayer_textField" placeholder="' + this.getLabel('communityPost') + '"></textarea></div><div class="sivaPlayer_media"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="19px" height="19px" viewBox="0 9 21 21"><polygon points="3,27 3,23.175 4.031,22.073 6.116,23.102 7.96,20.678 11.875,22.734 14.062,20.311 18,22.294 18,27" /><path d="M1,10v19h19V10H1z M19,28H2V11h17V28z" /><circle cx="17.018" cy="13.465" r="1.304" /></svg> <span>' + this.getLabel('communityAddMedia') + '</span><input name="media[]" type="file" /></div><div class="sivaPlayer_duration">' + this.getLabel('communityDuration') + ' <span><input type="text" name="durationFrom" class="sivaPlayer_start" value="' + this.formatTime(Math.floor(videoElement.currentTime)) + '" /> - <input type="text" name="durationTo" class="sivaPlayer_end" value="' + this.formatTime(videoElement.duration) + '" /></span></div><div class="sivaPlayer_visibility">' + this.getLabel('communityVisibility') + ' <select name="visibility"><option value="All">' + this.getLabel('communityVisibilityAll') + '</option><option value="Me">' + this.getLabel('communityVisibilityMe') + '</option><option value="Administrator">' + this.getLabel('communityVisibilityAdministrators') + '</option></select></div><div class="sivaPlayer_buttons"><button type="button" class="sivaPlayer_abort">' + this.getLabel('communityAbort') + '</button><button class="sivaPlayer_okay">' + this.getLabel('communityPublish') + '</button></div>');
		$('.sivaPlayer_media', editor).after($('.sivaPlayer_media', editor).clone(true).addClass('sivaPlayer_hidden'));
		$(editor).appendTo(this.player).fadeIn(600);
		$('input[type="file"]', editor).change(function(){
			$('.sivaPlayer_media.sivaPlayer_hidden', editor).clone(true).removeClass('sivaPlayer_hidden').insertAfter($(this).parent()).val('');
			if(thisPlayer.isValidExtension($(this).val(), SIVA_PLAYER_ALLOWED_COMMUNITY_EXTENSIONS) && this.files[0].size <= SIVA_PLAYER_ALLOWED_COMMUNITY_MAX_FILESIZE){
				var tmp = $(this).siblings('span').html($(this).val() + ' <span>(' + thisPlayer.getLabel('communityDeleteMedia') + ')</span>');
				$('span', tmp).click(function(){
					$(this).parent().closest('.sivaPlayer_media').remove();
				});
				$(this).css('visibility', 'hidden');
			}
			else{
				if(this.files[0].size > SIVA_PLAYER_ALLOWED_COMMUNITY_MAX_FILESIZE){
					alert(thisPlayer.getLabel('communityMediaSizeError') + parseInt(SIVA_PLAYER_ALLOWED_COMMUNITY_MAX_FILESIZE / 1024 / 1024) + 'MB');
				}
				else{				
					alert(thisPlayer.getLabel('communityMediaExtensionError') + SIVA_PLAYER_ALLOWED_COMMUNITY_EXTENSIONS.join(', '));
				}
				$(this).parent().remove();
			}
		});
		$('button', editor).click(function(e){
			e.preventDefault();
			thisPlayer.logAction('useButton', 'publish', 'thread');
			if($(this).hasClass('sivaPlayer_okay')){
				var title = $('.sivaPlayer_titleField', editor).val().trim();
				var text = $('.sivaPlayer_textField', editor).val().trim();
				var start = thisPlayer.timeToFloat($('.sivaPlayer_start', editor).val().trim());
				var end = thisPlayer.timeToFloat($('.sivaPlayer_end', editor).val().trim());
				if(title == ''){
					alert(thisPlayer.getLabel('communityTitleError'));
				}
				else if(text == ''){
					alert(thisPlayer.getLabel('communityPostError'));
				}
				else if(end - start < 2){
					alert(thisPlayer.getLabel('communityDurationError'));
				}
				else{
					thisPlayer.startLoader();
					$('.sivaPlayer_loader .sivaPlayer_text', thisPlayer.player).text(thisPlayer.getLabel('saving'));
					thisPlayer.setPlayedBefore();
					$.ajax({
						'async': true,
						'data': new FormData($('.sivaPlayer_annotationEditor', thisPlayer.player)[0]),
						'dataType': 'JSON',
						'crossDomain': true,
						'cache': false,
						'contentType': false,
						'processData': false,
						'timeout': 60000,
						'type': 'POST',
						'url': thisPlayer.appendAccessToken(thisPlayer.configuration.videoPath + '/createCollaborationThread.js?ajax=true&scene=' + thisPlayer.currentScene),
						'error': function(e){
							alert(thisPlayer.getLabel('communitySavingError') + '(' + e.statusText + ')');
							thisPlayer.stopLoader();
							thisPlayer.restorePlayedBefore();
						},
						'success': function(data){
							console.log(data);
							alert(thisPlayer.getLabel(data.message));
							if(data.saved){
								thisPlayer.getCommunityAnnotations(thisPlayer.currentScene, function(){
									thisPlayer.createAnnotations();
									$(editor).remove(0);
									thisPlayer.stopLoader();
									thisPlayer.restorePlayedBefore();
								});
							}
							else{
								thisPlayer.stopLoader();
								thisPlayer.restorePlayedBefore();
							}
						}
					});
				}
			}
			else{
				thisPlayer.logAction('useButton', 'abort', '');
				$(editor).fadeOut(600, function(){
					$(this).remove();
				});
			}
		});
		$('.sivaPlayer_start, .sivaPlayer_end', editor).focus(function(){
			$(this).select();
		})
		.change(function(){
			var val = thisPlayer.timeToFloat($(this).val());
			if(isNaN(val)){
				val = 0;
			}
			if(val < 0){
				val = 0;
			}
			else if(val > videoElement.duration){
				val = videoElement.duration;
			}
			if($(this).hasClass('sivaPlayer_start') && val > thisPlayer.timeToFloat($('.sivaPlayer_end', editor).val())){
				$('.sivaPlayer_end', editor).val(thisPlayer.formatTime(val));
			}
			else if($(this).hasClass('sivaPlayer_end') && val < thisPlayer.timeToFloat($('.sivaPlayer_start', editor).val())){
				$('.sivaPlayer_start', editor).val(thisPlayer.formatTime(val));
			}
			$(this).val(thisPlayer.formatTime(val));
		});
		this.setProportions();
	};
	
	this.createAnnotations = function(){
		var thisPlayer = this;
		if(this.configuration.common.isChromecastReceiver){
			return;
		}
		$('.sivaPlayer_annotation, .sivaPlayer_timelineMarker', this.player).remove();
		var annotations = [];
		for(var i = 0; i < this.configuration.globalAnnotations.length; i++){
			var annotation = this.configuration.annotations[(this.configuration.globalAnnotations[i])];
			annotations.push({'start': 0, 'annotation': annotation, 'triggerId': -1, 'tabs': ['all', 'author']});
		}
		for(var i = 0; i < this.configuration.scenes[this.currentScene].annotations.length; i++){
			var trigger = this.configuration.scenes[this.currentScene].annotations[i];
			var annotation = this.configuration.annotations[trigger.annotationId];
			annotations.push({'start': trigger.start, 'annotation': annotation, 'triggerId': trigger.triggerId, 'tabs': ['all', 'author']});
			if(annotation.type == 'markerButton' || annotation.type == 'markerEllipse' || annotation.type == 'markerRectangle' || annotation.type == 'markerPolygon'){
				var targetAnnotation = this.configuration.annotations[annotation.target];
				var tmpTriggerId = this.configuration.scenes[this.currentScene].annotations.length;
				this.configuration.scenes[this.currentScene].annotations.push({"start": -1.0, "end": -1.0, "annotationId": annotation.target, "triggerId": tmpTriggerId, "helper": true});
				this.configuration.annotations[annotation.target].triggerId = tmpTriggerId;
			}
		}
		if(this.configuration.common.collaboration){
			for(var i = 0; i < this.communityAnnotations.length; i++){
				annotations.push({'start': this.communityAnnotations[i].start, 'annotation': this.communityAnnotations[i], 'triggerId': -1, 'tabs': ['all', 'community']});
			}
		}
		annotations.sort(function(a, b){
			return a.start - b.start;
		});
		for(var i = 0; i < annotations.length; i++){
			this.createAnnotation(annotations[i].annotation, annotations[i].triggerId, annotations[i].tabs);
			if(annotations[i].start >= 1 && annotations[i].annotation.type != 'subTitle'){
				this.createTimelineMarker(annotations[i].start);
			}
		}
		$('.sivaPlayer_richtextImage', this.player).attr('src', function(index, src){
			$(this).attr('src', thisPlayer.configuration.videoPath + src);
		});
		$('.sivaPlayer_annotation.sivaPlayer_imageAnnotation img, .sivaPlayer_annotation.sivaPlayer_galleryAnnotation img', this.player).hover(function(){
			$(this).addClass('sivaPlayer_hover');
		}, function(){
			$(this).removeClass('sivaPlayer_hover');
		})
		.click(function(){
			var annotationId = thisPlayer.getAnnotationId($(this).closest('.sivaPlayer_annotation').attr('class'));
			thisPlayer.logAction('openImageAnnotation', annotationId, $(this).attr('src').replace(new RegExp(thisPlayer.configuration.videoPath, 'g'), '').split('?')[0]);
			var index = 0;
			var tmpThis = this;
			$.each($('img', $(this).closest('.sivaPlayer_annotation')), function(){
				if(this == tmpThis)
					return false;
				index++;
			});
			thisPlayer.createImageZoom(annotationId, index);
		});
		$('.sivaPlayer_annotation.sivaPlayer_imageAnnotation .sivaPlayer_title, .sivaPlayer_annotation.sivaPlayer_galleryAnnotation .sivaPlayer_title', this.player).click(function(){
			var annotationId = thisPlayer.getAnnotationId($(this).closest('.sivaPlayer_annotation').attr('class'));
			var image = $('img', $(this).closest('.sivaPlayer_annotation'))[0];
			thisPlayer.logAction('openImageAnnotation', annotationId, $(image).attr('src').replace(new RegExp(thisPlayer.configuration.videoPath, 'g'), '').split('?')[0]);
			thisPlayer.createImageZoom(annotationId, 0);
		});
		$('.sivaPlayer_pdfAnnotation', this.player).click(function(e){
			var annotation = $(this).closest('.sivaPlayer_annotation');
			var annotationId = thisPlayer.getAnnotationId($(annotation).attr('class'));
			var link = $('a', annotation);
			thisPlayer.logAction('openPdfAnnotation', annotationId, $(link).attr('href'));
			thisPlayer.createPdfZoom(annotationId);
			e.preventDefault();
		});
		$('.sivaPlayer_richtextAnnotation', this.player).click(function(e){
			var annotationId = thisPlayer.getAnnotationId($(this).attr('class'));
			thisPlayer.logAction('openRichtextAnnotation', annotationId, $('.sivaPlayer_title', this).text());
			thisPlayer.createRichtextZoom(annotationId);
			e.preventDefault();
		});
		$('.sivaPlayer_communityAnnotation', this.player).click(function(e){
			var annotationId = thisPlayer.getAnnotationId($(this).attr('class'));
			thisPlayer.logAction('openCommunityAnnotation', annotationId, $('.sivaPlayer_title', this).text());
			thisPlayer.createCommunityZoom(annotationId);
			e.preventDefault();
		});
		$('.sivaPlayer_annotation.sivaPlayer_videoAnnotation, .sivaPlayer_annotation.sivaPlayer_audioAnnotation', this.player).click(function(){
			var annotationId = thisPlayer.getAnnotationId($(this).attr('class'));
			thisPlayer.logAction('openMediaAnnotation', annotationId, 'openFullscreen');
			thisPlayer.createMediaZoom(annotationId, false);
		});
		$('.sivaPlayer_markerButton span, .sivaPlayer_markerEllipse ellipse, .sivaPlayer_markerRectangle rect, .sivaPlayer_markerPolygon polygon', this.player).hover(function(e){
			$(this).parent().parent().addClass('sivaPlayer_active');
			e.stopPropagation();
		}, function(e){
			$(this).parent().parent().removeClass('sivaPlayer_active');
			e.stopPropagation();
		})
		.click(function(e){
			thisPlayer.tidyPlayer(0, true);
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
	
	this.createAnnotation = function(annotation, triggerId, tabs){
		var content = '';
		var css = '';
		if(annotation.type == 'image'){
			content = '<img src="' + this.appendAccessToken(((annotation.preview) ? this.getThumbnailURL(this.configuration.videoPath + annotation.content[this.currentLanguage].href) : this.configuration.videoPath + annotation.content[this.currentLanguage].href)) + '" alt="' + this.getLabel('imageAnnotationAlt') + '" title="' + this.getLabel('imageAnnotationTooltip') + '" />';
			css = 'sivaPlayer_imageAnnotation';
		}
		else if(annotation.type == 'richText'){
			content = this.shortenText(annotation.content[this.currentLanguage].content, 150);
			css = 'sivaPlayer_richtextAnnotation';
		}
		else if(annotation.type == 'pdf'){
			var description = annotation.description;
			content = '<a href="' + this.appendAccessToken(this.configuration.videoPath + annotation.content[this.currentLanguage].href) + '" target="_blank" title="' + this.getLabel('pdfAnnotation_link') + '"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="20px" height="30px" viewBox="0 0 20 30"><polygon points="0,30 0,0 12.977,0 20,7.096 20,30" /></svg>' + ((description) ? description[this.currentLanguage].content : this.getLabel('pdfAnnotation_link')) +'</a>';
			css = 'sivaPlayer_pdfAnnotation';
		}
		else if(annotation.type == 'gallery'){
			content = '<table><tr>';
			var i = 0;
			for(; i < annotation.images.length; i++){
				if(i > 0 && i % annotation.columns == 0){
					content += '</tr><tr>';
				}
				content += '<td><img src="' + this.appendAccessToken(this.getThumbnailURL(this.configuration.videoPath + annotation.images[i][this.currentLanguage].href)) + '" alt="' + this.getLabel('imageAnnotationAlt') + '" title="' + this.getLabel('imageAnnotationTooltip') + '" class="sivaPlayer_galleryImage" /></td>';
			}
			for(; i % annotation.columns != 0; i++){
				content += '<td class="sivaPlayer_empty"></td>';
			}
			content += '</tr></table>';
			css = 'sivaPlayer_galleryAnnotation';
		}
		else if(annotation.type == 'video'){
			content = '<div class="sivaPlayer_mediaHolder">';
			if(annotation.thumbnail && annotation.thumbnail[this.currentLanguage]){
				content += '<img src="' + this.appendAccessToken(this.configuration.videoPath + annotation.thumbnail[this.currentLanguage].href) + '" />';
			}
			else{
				content += '<span class="sivaPlayer_placeholder"></span>';
			}
			content += '<span class="sivaPlayer_overlayButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="80px" height="80px" viewBox="0 0 300 300"><circle cx="150" cy="150" r="150"/><g class="sivaPlayer_playButton" title="' + this.getLabel('playTooltip') + '"><polygon points="108.5,82.767 108.5,225.856 232.5,154.31 "/></g></svg></span></div>';
			css = 'sivaPlayer_videoAnnotation';
		}
		else if(annotation.type == 'audio'){
			content = '<div class="sivaPlayer_mediaHolder"><span class="sivaPlayer_placeholder"></span><span class="sivaPlayer_overlayButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="80px" height="80px" viewBox="0 0 300 300"><circle cx="150" cy="150" r="150"/><g class="sivaPlayer_playButton" title="' + this.getLabel('playTooltip') + '"><polyline fill="none" stroke="#000000" stroke-width="0.5" stroke-miterlimit="10" points="7.866,19.284 9.852,19.284 10.116,17.527 10.91,20.306 11.307,16.999 12.232,21.829 13.158,14.485 13.951,20.702 15.01,11.208 16.068,22.227 16.731,14.618 17.788,20.57 18.45,16.329 19.241,20.438 19.774,18.321 20.169,21.829 20.964,19.284 22.946,19.284 "/><path d="M3.597,14.574C3.729,8.383,8.778,3.403,15,3.403s11.271,4.979,11.403,11.17h0.903C27.174,7.884,21.721,2.5,15,2.5 C8.279,2.5,2.825,7.884,2.693,14.574H3.597z"/><path d="M4.823,14.045c-0.186-0.004-0.369-0.022-0.559-0.003c-2.626,0.257-4.539,2.662-4.273,5.37 c0.265,2.708,2.608,4.694,5.234,4.437c0.189-0.019,0.366-0.071,0.547-0.111L4.823,14.045z"/><rect x="5.715" y="13.298" transform="matrix(0.9953 -0.0973 0.0973 0.9953 -1.8117 0.7539)" width="2.218" height="11.307"/><path d="M24.19,23.714c0.183,0.037,0.358,0.092,0.548,0.108c2.627,0.245,4.96-1.753,5.212-4.465 c0.253-2.708-1.673-5.102-4.299-5.345c-0.191-0.018-0.375,0-0.56,0.005L24.19,23.714z"/><path d="M23.5,7.649c-2.146-2.252-5.15-3.857-8.5-3.857c-3.444,0-6.531,1.692-8.688,4.045h0.171l1.799-0.152 c1.853-1.45,4.182-2.518,6.718-2.518c2.308,0,4.438,0.927,6.198,2.148L23.5,7.649z"/><rect x="22.016" y="13.253" transform="matrix(-0.9953 -0.0973 0.0973 -0.9953 44.3001 39.9748)" width="2.218" height="11.307"/><circle opacity="0.7" fill="#FFFFFF" cx="150" cy="150" r="150"/><path fill="#1D1D1B" d="M62.688,153.031c1.013-47.532,39.781-85.764,87.548-85.764c47.766,0,86.527,38.231,87.547,85.764h6.935	c-1.021-51.356-42.885-92.697-94.481-92.697s-93.477,41.341-94.482,92.697H62.688z"/><path fill="#1D1D1B" d="M72.096,148.969c-1.423-0.03-2.832-0.165-4.286-0.03c-20.154,1.979-34.842,20.437-32.802,41.228 c2.031,20.79,20.025,36.039,40.178,34.06c1.456-0.143,2.82-0.547,4.207-0.854L72.096,148.969z"/><polygon fill="#1D1D1B" points="100.167,229.01 83.223,230.666 74.751,144.274 91.697,142.609 "/><path fill="#1D1D1B" d="M220.793,223.198c1.401,0.286,2.751,0.706,4.204,0.833c20.169,1.882,38.08-13.457,40.014-34.278 c1.942-20.79-12.841-39.165-33.004-41.04c-1.468-0.136-2.878,0.007-4.294,0.045L220.793,223.198z"/><path fill="#1D1D1B" d="M215.492,99.868c-16.472-17.29-39.542-29.615-65.257-29.615c-26.452,0-50.144,12.993-66.705,31.055h1.313 l13.818-1.17c14.222-11.134,32.104-19.336,51.574-19.336c17.715,0,34.066,7.123,47.585,16.494L215.492,99.868z"/><polygon fill="#1D1D1B" points="199.904,228.664 216.848,230.321 225.321,143.929 208.376,142.265 "/></g></svg></span></div>';
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
		else if(annotation.type == 'community'){
			content = '';
			if(!annotation.posts[0].active){
				content += '<span class="sivaPlayer_activation">' + this.getLabel("communityNotActivated") + '</span>';
			}
			if(annotation.title != ''){
				content += '<span class="sivaPlayer_title">' + annotation.title + '</span>';
			}
			content += '<span class="sivaPlayer_common">' + this.getLabel('communityPostedBy') + ' <b>' + annotation.posts[0].user + '</b>, <span>' + this.timestampToDate(annotation.posts[0].date) + '</span></span>';
			if(annotation.posts[0].post != ''){
				content += '<span class="sivaPlayer_text">' + this.shortenText(annotation.posts[0].post, 200) + '</span>';
			}
			content += '<span class="sivaPlayer_info"><span class="sivaPlayer_answers">' + (annotation.posts.length - 1) + ' ' + this.getLabel((annotation.posts.length - 1 != 1) ? 'communityAnswers' : 'communityAnswer') + '</span><span class="sivaPlayer_media"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="80px" viewBox="0 0 30 80"><path stroke-width="2" stroke-miterlimit="10" d="M4.309,17.701c0,0,1.182,35.385,1.671,45.536 c0.09,1.862,6.793,7.46,9.531,7.46c1.957,0,9.444-6.476,9.847-8.112c0.938-3.812,0.198-49.976-1.428-53.2 c-1.243-2.467-12.883-1.55-14.534,0.367c-2.297,2.665,0.122,32.878,0.163,37.79c0.02,2.276,4.077,6.507,6.354,6.522 c2.019,0.015,5.652-3.803,5.703-5.951c0.184-7.746-0.815-31.717-0.815-31.717" /></svg> ' + annotation.posts[0].media.length + ' ' + this.getLabel((annotation.posts[0].media.length != 1) ? 'communityMediaFiles' : 'communityMediaFile') + '</span></span>';		
			css = 'sivaPlayer_communityAnnotation';
		}
		
		content = '<div class="sivaPlayer_annotation sivaPlayer_' + annotation.id + '_' + triggerId + ' ' + css + '">' + content + '</div>';
		
		if(annotation.title && annotation.title[this.currentLanguage] && annotation.title[this.currentLanguage].content != '' && !(annotation.type == 'subTitle' || annotation.type == 'markerButton' || annotation.type == 'markerEllipse' || annotation.type == 'markerRectangle')){
			content = $(content).prepend('<span class="sivaPlayer_title">' + annotation.title[this.currentLanguage].content + '</span>');
		}
		
		if(annotation.isSidebarAnnotation){
			for(var i = 0; i < tabs.length; i++){
				var header = $('.sivaPlayer_annotationSidebar .sivaPlayer_' + tabs[i] + 'Annotations .sivaPlayer_noAnnotation', this.player);
				$(header[header.length -1], this.player).after($(content).wrap('<p />').parent().html());
			}
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
		if(this.configuration.common.isChromecastReceiver){
			return;
		}
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
		for(var i = 0; i < this.communityAnnotations.length; i++){
			var annotation = this.communityAnnotations[i];
			annotations.push({'annotation': annotation, 'trigger': {'triggerId': -1, 'start': annotation.start, 'end': annotation.end}});
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
				var elements = $('.sivaPlayer_sidebarContents.sivaPlayer_active .sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ':hidden, .sivaPlayer_videoContainer .sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ':hidden', this.player);
				if((!annotation.isSidebarAnnotation || $('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId, this.player).parent().hasClass('sivaPlayer_active')) && elements.length > 0){
					$(elements).fadeIn({'duration': 500, 'done': function(a, t){
						if(a.isSidebarAnnotation){
							$('.sivaPlayer_annotationSidebar .sivaPlayer_scrollHolder', thisPlayer.player).scrollTop(0);
						}
						if(!a.isSidebarAnnotation || thisPlayer.isAnnotationSidebarVisible){
							if(a.pauseVideo){
								thisPlayer.pauseVideo();
							}
							if(a.muteVideo){
								mute = true;
							}
							if((a.type == 'video' || a.type == 'audio') && a.autostart){
								thisPlayer.createMediaZoom(a.id, true);
							}
						}
					}(annotation, trigger)});
				}
			}
			else{
				var elements = $('.sivaPlayer_sidebarContents.sivaPlayer_active .sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ':visible, .sivaPlayer_videoContainer .sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId + ':visible', this.player); 
				if(elements.length > 0){
					$(elements).fadeOut(500, function(a, t){
						$('.sivaPlayer_annotation.sivaPlayer_' + annotation.id + '_' + trigger.triggerId, thisPlayer.player).removeClass('sivaPlayer_active');
						if(a.isSidebarAnnotation){
							$('.sivaPlayer_annotationSidebar .sivaPlayer_scrollHolder', thisPlayer.player).scrollTop(0);
						}
						if(!a.isSidebarAnnotation || thisPlayer.isAnnotationSidebarVisible){
							if(a.muteVideo){
								unmute = true;
							}
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
	
	this.getCommunityAnnotations = function(scene, callback){
		var thisPlayer = this;
		if(this.configuration.common.collaboration){
			$.ajax({
				'async': true,
				'dataType': 'JSON',
				'data': {'scene': scene},
				'crossDomain': true,
				'timeout': 60000,
				'type': 'GET',
				'url': thisPlayer.appendAccessToken(thisPlayer.configuration.videoPath + '/getCollaboration.js?ajax=true'),
				'error': function(e){
					$('.sivaPlayer_annotationSidebar .sivaPlayer_allAnnotations, .sivaPlayer_annotationSidebar .sivaPlayer_communityAnnotations', thisPlayer.player).append('<div class="sivaPlayer_noAnnotation sivaPlayer_error sivaPlayer_button"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="40px" height="40px" viewBox="0 0 100 100" xml:space="preserve"><path stroke-width="0.25" stroke-miterlimit="10" d="M51.5,4L4,99h95L51.5,4z M51.5,17.5l37.5,75 H14L51.5,17.5z"/><path d="M49.638,71.813l-1.711-17.438v-8.063h7.125v8.063l-1.688,17.438H49.638z M48.208,80.672v-6.586h6.586v6.586H48.208z"/></svg> ' + thisPlayer.getLabel('communityFetchingError') + e.statusText + '</div>');
					callback();
				},
				'success': function(data){
					if(data.message){
						$('.sivaPlayer_annotationSidebar .sivaPlayer_allAnnotations, .sivaPlayer_annotationSidebar .sivaPlayer_communityAnnotations', thisPlayer.player).append('<div class="sivaPlayer_noAnnotation sivaPlayer_error sivaPlayer_button"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="40px" height="40px" viewBox="0 0 100 100" xml:space="preserve"><path stroke-width="0.25" stroke-miterlimit="10" d="M51.5,4L4,99h95L51.5,4z M51.5,17.5l37.5,75 H14L51.5,17.5z"/><path d="M49.638,71.813l-1.711-17.438v-8.063h7.125v8.063l-1.688,17.438H49.638z M48.208,80.672v-6.586h6.586v6.586H48.208z"/></svg> ' + thisPlayer.getLabel('communityFetchingError') + data.message + '</div>');
					}
					else{
						for(var i = 0; i < data.length; i++){
							data[i].id = 'show-CommunityAnnotation_' + i;
							data[i].type = 'community';
							data[i].isSidebarAnnotation = true;
						}
						thisPlayer.communityAnnotations = data;
					}
					callback();
				}
			});
		}
		else{
			callback();
		}
	};
	
	this.createMediaAnnotationControls = function(autostarted){
		var thisPlayer = this;
		$.each($('.sivaPlayer_mediaPopup video, .sivaPlayer_mediaPopup audio', this.player), function(){
			var mediaElement = this;
			var duration = this.duration;
			if(!duration || isNaN(duration)){
				duration = 0;
			}
			$(this).after('<table class="sivaPlayer_mediaAnnotationControls"><tr><td class="sivaPlayer_button sivaPlayer_playPauseButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="20px" height="20px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_playButton" title="' + thisPlayer.getLabel('playTooltip') + '"><polygon points="2,0 2,30 28,15"/></g><g class="sivaPlayer_pauseButton" title="' + thisPlayer.getLabel('pauseTooltip') + '"><rect x="2" width="10.4" height="30"/><rect x="17.6" width="10.4" height="30"/></g><g class="sivaPlayer_replayButton" title="' + thisPlayer.getLabel('replayTooltip') + '"><path d="M14.928,1.838c-2.375,0-4.605,0.599-6.566,1.64L4.192,0L2.209,14.822l12.707-5.878l-3.802-3.17 c1.188-0.448,2.469-0.706,3.813-0.706c5.992,0,10.852,4.857,10.852,10.852c0,5.994-4.859,10.851-10.852,10.851 c-5.12,0-9.401-3.55-10.542-8.32l-3.395-0.622C1.924,24.699,7.799,30,14.928,30c7.777,0,14.082-6.304,14.082-14.081 C29.01,8.142,22.705,1.838,14.928,1.838z"/></g></svg></td><td class="sivaPlayer_timelineCurrentTime">00:00</td><td class="sivaPlayer_timeline"><span class="sivaPlayer_timelineProgress"><span class="sivaPlayer_timelineProgressBar"><span></span></span></span></td><td class="sivaPlayer_timelineDuration">' + thisPlayer.formatTime(duration) + '</td></tr></table>');
			var mediaHolder = $(this).parent();
			$(this).prependTo(mediaHolder);
			if($(this).prop('tagName').toLowerCase() == 'video'){
				$(mediaHolder).append('<span class="sivaPlayer_overlayButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="80px" height="80px" viewBox="0 0 300 300"><circle cx="150" cy="150" r="150"/><g class="sivaPlayer_playButton" title="' + thisPlayer.getLabel('playTooltip') + '"><polygon points="108.5,82.767 108.5,225.856 232.5,154.31 "/></g><g class="sivaPlayer_replayButton" title="' + thisPlayer.getLabel('replayTooltip') + '"><path d="M149.539,64.941c-15.144,0-29.366,3.874-41.87,10.596L81.087,53.065l-12.647,95.786l81.024-37.991 l-24.24-20.486c7.577-2.896,15.741-4.557,24.315-4.557c38.209,0,69.193,31.392,69.193,70.128c0,38.73-30.984,70.12-69.193,70.12 c-32.647,0-59.939-22.94-67.226-53.769l-21.645-4.021c5.953,44.404,43.414,78.658,88.87,78.658 c49.592,0,89.792-40.736,89.792-90.989C239.331,105.678,199.131,64.941,149.539,64.941z"/></g></svg></span>');
				$(mediaHolder).click(function(){
					var annotationId = thisPlayer.getAnnotationId($(mediaElement).attr('class'));
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
			else{
				$(mediaHolder).append('<span class="sivaPlayer_overlayButton"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="80px" height="80px" viewBox="0 0 300 300"><circle cx="150" cy="150" r="150"/><g title="' + thisPlayer.getLabel('playTooltip') + '"><circle opacity="0.7" fill="#FFFFFF" cx="150" cy="150" r="150"/><path fill="#1D1D1B" d="M62.688,153.031c1.013-47.532,39.781-85.764,87.548-85.764c47.766,0,86.527,38.231,87.547,85.764h6.935	c-1.021-51.356-42.885-92.697-94.481-92.697s-93.477,41.341-94.482,92.697H62.688z"/><path fill="#1D1D1B" d="M72.096,148.969c-1.423-0.03-2.832-0.165-4.286-0.03c-20.154,1.979-34.842,20.437-32.802,41.228 c2.031,20.79,20.025,36.039,40.178,34.06c1.456-0.143,2.82-0.547,4.207-0.854L72.096,148.969z"/><polygon fill="#1D1D1B" points="100.167,229.01 83.223,230.666 74.751,144.274 91.697,142.609 "/><path fill="#1D1D1B" d="M220.793,223.198c1.401,0.286,2.751,0.706,4.204,0.833c20.169,1.882,38.08-13.457,40.014-34.278 c1.942-20.79-12.841-39.165-33.004-41.04c-1.468-0.136-2.878,0.007-4.294,0.045L220.793,223.198z"/><path fill="#1D1D1B" d="M215.492,99.868c-16.472-17.29-39.542-29.615-65.257-29.615c-26.452,0-50.144,12.993-66.705,31.055h1.313 l13.818-1.17c14.222-11.134,32.104-19.336,51.574-19.336c17.715,0,34.066,7.123,47.585,16.494L215.492,99.868z"/><polygon fill="#1D1D1B" points="199.904,228.664 216.848,230.321 225.321,143.929 208.376,142.265 "/></g></svg></span>');
				$(mediaHolder).click(function(){
					var annotationId = thisPlayer.getAnnotationId($(mediaElement).attr('class'));
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
				$('.sivaPlayer_mediaPopup .sivaPlayer_timelineDuration', thisPlayer.player).text(thisPlayer.formatTime(this.duration));				
			})
			.bind('play', function(){
				if(this.ended){
					this.currentTime = 0;
					this.ended = false;
				}
				$('.sivaPlayer_mediaPopup .sivaPlayer_playButton, .sivaPlayer_mediaPopup .sivaPlayer_replayButton', thisPlayer.player).hide(0);
				if($(this).prop('tagName').toLowerCase() == 'video'){
					$('.sivaPlayer_mediaPopup .sivaPlayer_overlayButton').hide(0);
				}
				$('.sivaPlayer_mediaPopup .sivaPlayer_pauseButton', thisPlayer.player).show(0);
				$('.sivaPlayer_mediaPopup .sivaPlayer_overlayButton').addClass('sivaPlayer_playing');
				thisPlayer.updateMediaAnnotationTimelines(false);
			})
			.bind('pause', function(){
				if(!this.ended){
					$('.sivaPlayer_mediaPopup .sivaPlayer_playButton, .sivaPlayer_mediaPopup .sivaPlayer_overlayButton', thisPlayer.player).show(0);
					$('.sivaPlayer_mediaPopup .sivaPlayer_pauseButton', thisPlayer.player).hide(0);
				}
				$('.sivaPlayer_mediaPopup .sivaPlayer_overlayButton').removeClass('sivaPlayer_playing');
				thisPlayer.playingMediaAnnotations.pop();
			})
			.bind('ended', function(){
				this.ended = true;
				this.pause();
				var annotationId = thisPlayer.getAnnotationId($(mediaElement).attr('class'));
				thisPlayer.logAction('manageMediaAnnotation', annotationId, 'end');
				$('.sivaPlayer_mediaPopup .sivaPlayer_replayButton, .sivaPlayer_mediaPopup .sivaPlayer_overlayButton', thisPlayer.player).show(0);
				$('.sivaPlayer_mediaPopup .sivaPlayer_playButton, .sivaPlayer_mediaPopup .sivaPlayer_pauseButton', thisPlayer.player).hide(0);
				if(autostarted){
					thisPlayer.tidyPlayer(800, false, true);
				}
			});		
			var controls = $('.sivaPlayer_mediaPopup .sivaPlayer_mediaAnnotationControls', thisPlayer.player).click(function(e){
				e.stopPropagation();
			});
			$('.sivaPlayer_playPauseButton', controls).click(function(){
				var annotationId = thisPlayer.getAnnotationId($(mediaElement).attr('class'));
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
				console.log(mediaElement.paused);
				var percentage = (e.pageX - $(this).offset().left) / $(this).width();
				var duration = mediaElement.duration;
				var annotationId = thisPlayer.getAnnotationId($(mediaElement).attr('class'));
				thisPlayer.logAction('manageMediaAnnotation', annotationId, 'seek');
				mediaElement.currentTime = duration * percentage;
				console.log(mediaElement.paused);
				if(mediaElement.paused){
					$('.sivaPlayer_mediaPopup .sivaPlayer_playButton, .sivaPlayer_mediaPopup .sivaPlayer_overlayButton', thisPlayer.player).show(0);
					$('.sivaPlayer_mediaPopup .sivaPlayer_replayButton, .sivaPlayer_mediaPopup .sivaPlayer_pauseButton', thisPlayer.player).hide(0);
				}
				thisPlayer.updateMediaAnnotationTimelines(true);
			});
			this.volume = thisPlayer.volume;
		});
	};
	
	this.updateMediaAnnotationTimelines = function(alwaysUpdate){
		var thisPlayer = this;
		$.each($('.sivaPlayer_mediaPopup video, .sivaPlayer_mediaPopup audio', this.player), function(){
			var currentTime = this.currentTime;
			var duration = this.duration;
			if(!duration || isNaN(duration)){
				return;
			}
			var percentage = (parseInt((currentTime * 100 / duration) * 100) / 100);
			if(percentage > 100){
				percentage = 100;
			}
			$('.sivaPlayer_mediaPopup .sivaPlayer_timelineProgress .sivaPlayer_timelineProgressBar span', thisPlayer.player).css('width', percentage + '%');
			$('.sivaPlayer_mediaPopup .sivaPlayer_timelineCurrentTime', thisPlayer.player).text(thisPlayer.formatTime(currentTime));
		});
		if(!alwaysUpdate && !this.paused){
			setTimeout(function(){
				thisPlayer.updateMediaAnnotationTimelines(false);
			}, 500);
		}
	};
	
	this.createMediaZoom = function(annotationId, autostarted){
		var thisPlayer = this;
		this.setPlayedBefore();
		if(!autostarted){
			this.sendChromecastMessage('createMediaZoom', [annotationId]);
		}
		var annotation = this.configuration.annotations[annotationId];
		this.createPopup('media', true);
		$('.sivaPlayer_mediaPopup .sivaPlayer_closeButton', this.player).click(function(){
			thisPlayer.logAction('closeMediaAnnotation', '', '');
		});
		if(annotation.title && annotation.title[this.currentLanguage])
		$('.sivaPlayer_mediaPopup .sivaPlayer_title', this.player).text(annotation.title[this.currentLanguage].content);		
		var content;
		if(annotation.type == 'video'){
			content = '<video class="sivaPlayer_' + annotationId + '">';
			for(var i = 0; i < annotation.files.length; i++){
				content += '<source src="' + this.appendAccessToken(this.configuration.videoPath + annotation.files[i].url[this.currentLanguage].href + '.' + annotation.files[i].format) + '" type="' + annotation.files[i].type + '" />';
			}
			content += '</video>';
		}
		else{
			content = '<audio class="sivaPlayer_' + annotationId + '">';
			for(var i = 0; i < annotation.files.length; i++){
				content += '<source src="' + this.appendAccessToken(this.configuration.videoPath + annotation.files[i].url[this.currentLanguage].href + '.' + annotation.files[i].format) + '" type="' + annotation.files[i].type + '" />';
			}
			content += '</audio>';
		}
		$('.sivaPlayer_mediaPopup .sivaPlayer_content', this.player).html(content);
		this.createMediaAnnotationControls(autostarted);
		this.setProportions();
		if(this.configuration.common.isChromecastReceiver){
			var mediaManager = new cast.receiver.MediaManager($('.sivaPlayer_mediaPopup video, .sivaPlayer_mediaPopup audio', this.player)[0]);
		}
		$('.sivaPlayer_mediaPopup video, .sivaPlayer_mediaPopup audio', this.player)[0].play();
	};
	
	this.createPdfZoom = function(annotationId){
		var thisPlayer = this;
		this.setPlayedBefore();
		this.sendChromecastMessage('createPdfZoom', [annotationId]);
		var annotation = this.configuration.annotations[annotationId];
		this.createPopup('pdf', true);
		$('.sivaPlayer_pdfPopup .sivaPlayer_closeButton', this.player).click(function(){
			thisPlayer.logAction('closePdfAnnotation', '', '');
		});
		if(annotation.title && annotation.title[this.currentLanguage]){
			$('.sivaPlayer_pdfPopup .sivaPlayer_title', this.player).text(annotation.title[this.currentLanguage].content);
		}
		$('.sivaPlayer_pdfPopup .sivaPlayer_content', this.player).empty().append(this.getLabel('pdfAnnotation_error') + '<iframe src="' + this.appendAccessToken(this.configuration.videoPath + annotation.content[this.currentLanguage].href) + '"></iframe>');
	};
	
	this.createRichtextZoom = function(annotationId){
		var thisPlayer = this;
		this.setPlayedBefore();
		var annotation = this.configuration.annotations[annotationId];
		this.sendChromecastMessage('createRichtextZoom', [annotationId]);
		this.createPopup('richtext', true);
		$('.sivaPlayer_richtextPopup .sivaPlayer_closeButton', this.player).click(function(){
			thisPlayer.logAction('closeRichtextAnnotation', '', '');
		});
		if(annotation.title && annotation.title[this.currentLanguage]){
			$('.sivaPlayer_richtextPopup .sivaPlayer_title', this.player).text(annotation.title[this.currentLanguage].content);
		}
		$('.sivaPlayer_richtextPopup .sivaPlayer_scrollable', this.player).empty().append(annotation.content[this.currentLanguage].content);
		$('.sivaPlayer_richtextPopup .sivaPlayer_scrollable .sivaPlayer_richtextImage', this.player).attr('src', function(index, src){
			$(this).attr('src', thisPlayer.configuration.videoPath + src);
		});
	};
	
	this.createCommunityZoom = function(annotationId){
		var thisPlayer = this;
		this.setPlayedBefore();
		this.sendChromecastMessage('createCommunityZoom', [annotationId]);
		var annotation = {};
		for(var i = 0; i < this.communityAnnotations.length; i++){
			if(this.communityAnnotations[i].id == annotationId){
				annotation = this.communityAnnotations[i];
				break;
			}
		}
		thisPlayer.createPopup('community', true);
		$('.sivaPlayer_communityPopup .sivaPlayer_closeButton', this.player).click(function(){
			thisPlayer.logAction('closeCommunityAnnotation', '', '');
		});
		if(annotation.title){
			$('.sivaPlayer_communityPopup .sivaPlayer_title', this.player).text(annotation.title);
		}
		$('.sivaPlayer_communityPopup .sivaPlayer_scrollable', this.player).empty().append('<div class="sivaPlayer_post"><span class="sivaPlayer_common">' + this.getLabel('communityPostedBy') + ' <b>' + annotation.posts[0].user + '</b>, <span>' + this.timestampToDate(annotation.posts[0].date) + '</span></span><span>' + this.textToHTML(annotation.posts[0].post) + '</span></div><form class="sivaPlayer_answers" enctype="multipart/form-data"><div class="sivaPlayer_title2">' + this.getLabel('communityAnswers') + '</div></form>');
		if(annotation.posts[0].manageable){
			$('.sivaPlayer_communityPopup .sivaPlayer_post').prepend('<a class="sivaPlayer_administration sivaPlayer_deleteThread" href="' + this.appendAccessToken(this.configuration.videoPath + "/deleteCollaborationThread.js?threadId=" + annotation.threadId) + '">' + this.getLabel('communityDelete') + '</a>');
		}
		if(!annotation.posts[0].active){
			$('.sivaPlayer_communityPopup .sivaPlayer_post').prepend('<span class="sivaPlayer_activation">' + this.getLabel('communityNotActivated') + '</span><a class="sivaPlayer_administration sivaPlayer_activatePost" href="' + this.appendAccessToken(this.configuration.videoPath + "/activateCollaborationPost.js?postId=" + annotation.posts[0].id) + '">' + this.getLabel('communityActivate') + '</a>');
		}
		if(annotation.posts[0].media.length > 0){
			var media = '';
			for(var i = 0; i < annotation.posts[0].media.length; i++){
				var filename = this.getFilename(annotation.posts[0].media[i].filename).split('-');
				filename.shift();
				filename = filename.join('-');
				media += '<a href="' + this.appendAccessToken(this.configuration.videoPath + '/' + annotation.posts[0].media[i].filename) + '" target="_blank">' + this.getFilename(filename) + '</a>';
			}
			$('.sivaPlayer_communityPopup .sivaPlayer_post', this.player).append('<span class="sivaPlayer_media"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="80px" viewBox="0 0 30 80"><path stroke-width="2" stroke-miterlimit="10" d="M4.309,17.701c0,0,1.182,35.385,1.671,45.536 c0.09,1.862,6.793,7.46,9.531,7.46c1.957,0,9.444-6.476,9.847-8.112c0.938-3.812,0.198-49.976-1.428-53.2 c-1.243-2.467-12.883-1.55-14.534,0.367c-2.297,2.665,0.122,32.878,0.163,37.79c0.02,2.276,4.077,6.507,6.354,6.522 c2.019,0.015,5.652-3.803,5.703-5.951c0.184-7.746-0.815-31.717-0.815-31.717" /></svg> ' + media + '</span>');
		}
		for(var i = 1; i < annotation.posts.length; i++){
			var media = '';
			if(annotation.posts[i].media.length > 0){
				for(var j = 0; j < annotation.posts[i].media.length; j++){
					var filename = this.getFilename(annotation.posts[i].media[j].filename).split('-');
					filename.shift();
					filename = filename.join('-');
					media += '<a href="' + this.appendAccessToken(this.configuration.videoPath + "/" + annotation.posts[i].media[j].filename) + '" target="_blank">' + filename + '</a>';
				}
				media = '<span class="sivaPlayer_media"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="80px" viewBox="0 0 30 80"><path stroke-width="2" stroke-miterlimit="10" d="M4.309,17.701c0,0,1.182,35.385,1.671,45.536 c0.09,1.862,6.793,7.46,9.531,7.46c1.957,0,9.444-6.476,9.847-8.112c0.938-3.812,0.198-49.976-1.428-53.2 c-1.243-2.467-12.883-1.55-14.534,0.367c-2.297,2.665,0.122,32.878,0.163,37.79c0.02,2.276,4.077,6.507,6.354,6.522 c2.019,0.015,5.652-3.803,5.703-5.951c0.184-7.746-0.815-31.717-0.815-31.717" /></svg> ' + media + '</span>';
			}
			$('.sivaPlayer_communityPopup .sivaPlayer_answers', this.player).append('<div class="sivaPlayer_answer">' + ((!annotation.posts[i].active) ? '<span class="sivaPlayer_activation">' + this.getLabel('communityNotActivated') + '</span><a class="sivaPlayer_administration sivaPlayer_activatePost" href="' + this.appendAccessToken(this.configuration.videoPath + "/activateCollaborationPost.js?postId=" + annotation.posts[i].id) + '">' + this.getLabel('communityActivate') + '</a>' : '') + ((annotation.posts[i].manageable) ? '<a class="sivaPlayer_administration sivaPlayer_deletePost" href="' + this.appendAccessToken(this.configuration.videoPath + "/deleteCollaborationPost.js?postId=" + annotation.posts[i].id) + '">' + this.getLabel('communityDelete') + '</a>' : '') + '<span class="sivaPlayer_common">' + this.getLabel('communityPostedBy') + ' <b>' + annotation.posts[i].user + '</b>, <span>' + this.timestampToDate(annotation.posts[i].date) + '</span></span><span>' + this.textToHTML(annotation.posts[i].post) + '</span>' + (media) + '</div></div>');
		}
		$('.sivaPlayer_communityPopup .sivaPlayer_answers', this.player).append('<div class="sivaPlayer_newAnswer"><textarea name="post" placeholder="' + this.getLabel('communityNewAnswer') + '"></textarea><span class="sivaPlayer_addMedia"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="19px" height="19px" viewBox="0 9 21 21"><polygon points="3,27 3,23.175 4.031,22.073 6.116,23.102 7.96,20.678 11.875,22.734 14.062,20.311 18,22.294 18,27" /><path d="M1,10v19h19V10H1z M19,28H2V11h17V28z" /><circle cx="17.018" cy="13.465" r="1.304" /></svg> <span>' + this.getLabel('communityAddMedia') + '</span><input type="file" name="media[]" /></span><button class="sivaPlayer_okay">' + this.getLabel('communityPublish') + '</button></div>');
		$('.sivaPlayer_communityPopup .sivaPlayer_answers .sivaPlayer_addMedia', this.player).after($('.sivaPlayer_communityPopup .sivaPlayer_answers .sivaPlayer_addMedia', this.player).clone(true).addClass('sivaPlayer_hidden'));
		$('.sivaPlayer_communityPopup .sivaPlayer_answers input[type="file"]', this.player).change(function(){
			$('.sivaPlayer_communityPopup .sivaPlayer_answers .sivaPlayer_addMedia.sivaPlayer_hidden', this.player).clone(true).removeClass('sivaPlayer_hidden').insertAfter($(this).parent()).val('');
			if(thisPlayer.isValidExtension($(this).val(), SIVA_PLAYER_ALLOWED_COMMUNITY_EXTENSIONS) && this.files[0].size <= SIVA_PLAYER_ALLOWED_COMMUNITY_MAX_FILESIZE){
				var tmp = $(this).siblings('span').html($(this).val() + ' <span>(' + thisPlayer.getLabel('communityDeleteMedia') + ')</span>');
				$('span', tmp).click(function(){
					$(this).parent().closest('.sivaPlayer_addMedia').remove();
				});
				$(this).css('visibility', 'hidden');
			}
			else{
				if(this.files[0].size > SIVA_PLAYER_ALLOWED_COMMUNITY_MAX_FILESIZE){
					alert(thisPlayer.getLabel('communityMediaSizeError') + parseInt(SIVA_PLAYER_ALLOWED_COMMUNITY_MAX_FILESIZE / 1024 / 1024) + 'MB');
				}
				else{				
					alert(thisPlayer.getLabel('communityMediaExtensionError') + SIVA_PLAYER_ALLOWED_COMMUNITY_EXTENSIONS.join(', '));
				}
				$(this).parent().closest('.sivaPlayer_addMedia').remove();
			}
		});
		$('.sivaPlayer_communityPopup .sivaPlayer_answers button', this.player).click(function(e){
			thisPlayer.logAction('useButton', 'publish', 'answer');
			var text = $('.sivaPlayer_communityPopup .sivaPlayer_answers textarea', thisPlayer.player).val().trim();
			if(text == ''){
				alert('Bitte geben Sie Ihre Antwort ein.');
			}
			else{
				thisPlayer.startLoader();
				$('.sivaPlayer_loader .sivaPlayer_text', thisPlayer.player).text(thisPlayer.getLabel('saving'));
				$.ajax({
					'async': true,
					'data': new FormData($('.sivaPlayer_communityPopup form', thisPlayer.player)[0]),
					'dataType': 'JSON',
					'crossDomain': true,
					'cache': false,
					'contentType': false,
					'processData': false,
					'timeout': 60000,
					'type': 'POST',
					'url': thisPlayer.appendAccessToken(thisPlayer.configuration.videoPath + '/createCollaborationPost.js?ajax=true&scene=' + thisPlayer.currentScene + '&thread=' +  annotation.threadId),
					'error': function(e){
						alert(thisPlayer.getLabel('communitySavingError') + '(' + e.statusText + ')');
						thisPlayer.stopLoader();
					},
					'success': function(data){
						alert(thisPlayer.getLabel(data.message));
						if(data.saved){
							thisPlayer.getCommunityAnnotations(thisPlayer.currentScene, function(){
								thisPlayer.createAnnotations();
								$('.sivaPlayer_communityPopup', thisPlayer.player).remove();
								thisPlayer.createCommunityZoom(annotationId);
								thisPlayer.stopLoader();
							});
						}
						else{
							thisPlayer.stopLoader();
						}
					}
				});
			}
			e.preventDefault();
		});
		$('.sivaPlayer_communityPopup .sivaPlayer_administration', this.player).click(function(e){
			var deleteThread = $(this).hasClass('sivaPlayer_deleteThread');
			if($(this).hasClass('sivaPlayer_activatePost') || ($(this).hasClass('sivaPlayer_deleteThread') && confirm(thisPlayer.getLabel('communityDeleteThreadConfirm'))) || ($(this).hasClass('sivaPlayer_deletePost') && confirm(thisPlayer.getLabel('communityDeletePostConfirm')))){
				if($(this).hasClass('sivaPlayer_activatePost')){
					thisPlayer.logAction('manageCommunityAnnotation', 'activate', '');
				}
				else if($(this).hasClass('sivaPlayer_deleteThread')){
					thisPlayer.logAction('manageCommunityAnnotation', 'delete', 'thread');
				}
				else if($(this).hasClass('sivaPlayer_deletePost')){
					thisPlayer.logAction('manageCommunityAnnotation', 'delete', 'answer');
				}
				thisPlayer.startLoader();
				$('.sivaPlayer_loader .sivaPlayer_text', thisPlayer.player).text(thisPlayer.getLabel('saving'));
				$.ajax({
					'async': true,
					'dataType': 'JSON',
					'crossDomain': true,
					'timeout': 60000,
					'type': 'GET',
					'url': $(this).attr('href'),
					'error': function(e){
						alert(thisPlayer.getLabel('communitySavingError') + '(' + e.statusText + ')');
						thisPlayer.stopLoader();
					},
					'success': function(data){
						if(data.saved){
							thisPlayer.getCommunityAnnotations(thisPlayer.currentScene, function(){
								thisPlayer.createAnnotations();
								$('.sivaPlayer_communityPopup', thisPlayer.player).remove();
								if(!deleteThread){
									thisPlayer.createCommunityZoom(annotationId);
								}
								thisPlayer.stopLoader();
							});
						}
						else{
							thisPlayer.stopLoader();
						}
					}
				});
			}
			e.preventDefault();
		});
	};
	
	this.createImageZoom = function(annotationId, index){
		var thisPlayer = this;
		this.sendChromecastMessage('createImageZoom', [annotationId, index]);
		thisPlayer.setPlayedBefore();
		thisPlayer.createPopup('zoom', true);
		var annotation = this.configuration.annotations[annotationId];
		$('.sivaPlayer_zoomPopup .sivaPlayer_closeButton', this.player).click(function(){
			thisPlayer.logAction('closeImageAnnotation', '', '');
		});
		if(annotation.title && annotation.title[this.currentLanguage]){
			$('.sivaPlayer_zoomPopup .sivaPlayer_title', this.player).text(annotation.title[this.currentLanguage].content);
		}
		$('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).empty().append('<img src="' + this.appendAccessToken(this.configuration.videoPath + ((annotation.type == 'gallery') ? annotation.images[index][this.currentLanguage].href : annotation.content[this.currentLanguage].href)) + '" alt="' + this.getLabel('zoomImageTooltip') + '" />');
		if(annotation.type == 'gallery'){
			var thumbnails = '';
			for(var i = 0; i < annotation.images.length; i++){
				thumbnails += '<img src="' + this.appendAccessToken(this.getThumbnailURL(this.configuration.videoPath + annotation.images[i][this.currentLanguage].href)) + '" />';
			}
			$('.sivaPlayer_zoomPopup tr:last-child', this.player).after('<tr class="sivaPlayer_galleryThumbnails"><td colspan="3"><div>' + thumbnails + '</div></td></tr>');
			$('.sivaPlayer_galleryThumbnails img', this.player).hover(function(){
				$(this).addClass('sivaPlayer_hover');
			}, function(){
				$(this).removeClass('sivaPlayer_hover');
			})
			.click(function(){
				thisPlayer.logAction('changeOpenedImage', 'imageClick', $(this).attr('src'));
				var pos = 0;
				var tmpThis = this;
				$.each($('.sivaPlayer_galleryThumbnails img', thisPlayer.player), function(){
					if(this == tmpThis)
						return false;
					pos++;
				});
				thisPlayer.setZoomImage(pos);
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
			$('.sivaPlayer_zoomPopup .sivaPlayer_closeButton', this.player).click(function(){
				$(document).unbind('keydown')
				.keydown(function(e) {
				    if(e.keyCode == 32) {
				        return false;
				    }
				});
			});
			$('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player).append(nextButton);
			$(document).keydown(function(e){
				if(e.keyCode == '37'){
					thisPlayer.setPreviousZoomImage();
				}
				else if(e.keyCode == '39'){
					thisPlayer.setNextZoomImage();
				}
			});
			$('.sivaPlayer_zoomPopup .sivaPlayer_content', this.player)
			.bind('swipeleft', function(e){
				thisPlayer.setNextZoomImage();
			})
			.bind('swiperight', function(e){
				thisPlayer.setPreviousZoomImage();
			});
			
			thisPlayer.setProportions();
			this.setZoomImage(index);
		}
		else{
			$('.sivaPlayer_zoomPopup tr:last-child', this.player).after('<tr class="sivaPlayer_spacer"><td colspan="3"></td></tr>');
		}
	};
	
	this.setNextZoomImage = function(){
		var images = $('.sivaPlayer_galleryThumbnails img', this.player);
		var i = 0;
		for( ; i < images.length; i++){
			if($(images[i]).hasClass('sivaPlayer_current')){
				break;
			}
		}
		i = (i + 1) % images.length;
		this.logAction('changeOpenedImage', 'nextButton', $(images[i]).attr('src'));
		this.setZoomImage(i);
	};
	
	this.setPreviousZoomImage = function(){
		var images = $('.sivaPlayer_galleryThumbnails img', this.player);
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
		this.logAction('changeOpenedImage', 'prevButton', $(images[i]).attr('src'));
		this.setZoomImage(i);
	};
	
	this.setZoomImage = function(index){
		this.sendChromecastMessage('setZoomImage', [index]);
		var thumbnails = $('.sivaPlayer_galleryThumbnails img', this.player);
		$(thumbnails).removeClass('sivaPlayer_current');
		$(thumbnails[index]).addClass('sivaPlayer_current');
		$('.sivaPlayer_zoomPopup .sivaPlayer_content img', this.player).attr('src', this.getOriginalImageURL($(thumbnails[index]).attr('src')));
		var parent = $(thumbnails[index]).parent();
		if($(thumbnails[index]).offset().left < $(parent).offset().left || $(thumbnails[index]).offset().left + $(thumbnails[index]).width() > $(parent).offset().left + $(parent).width()){
			$(parent).scrollLeft($(thumbnails[index]).offset().left - $(parent).offset().left);
		}
	};
	
	this.getThumbnailURL = function(src){
		src = src.split('?token=')[0].split('.');
		var extension = src.pop();
		return this.appendAccessToken(src.join('.') + '_thumb.' + extension); 
	};
	
	this.getOriginalImageURL = function(src){
		if(src.indexOf('_thumb.') == -1)
			return src;
		src = src.split('?token=')[0].split('_thumb.');
		var extension = src.pop();
		return this.appendAccessToken(src.join('_thumb.') + '.' + extension);
	};
	
	this.appendAccessToken = function(src, alwaysUseToken, useUserSecret){
		if(this.configuration.accessRestriction && this.configuration.accessRestriction.accessToken){
			src += ((src.indexOf('?') > -1) ? '&' : '?') + 'token=' + this.configuration.accessRestriction.accessToken;
		}
		else if(alwaysUseToken){
			src += ((src.indexOf('?') > -1) ? '&' : '?') + 'token2=' + this.logKey;
		}
		if(useUserSecret){
			src += ((src.indexOf('?') > -1) ? '&' : '?') + '&email=' + ((this.configuration.common.userEmail) ? this.configuration.common.userEmail : '') + '&secret=' + ((this.configuration.common.userSecret) ? this.configuration.common.userSecret : '');
		}
		return src;
	};
	
	this.getFilename = function(url){
		return url.split('/').pop();
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
		var milliSeconds = 0;
		if(time.length > 1){
			milliSeconds = parseInt(time[1]) / 1000;
		}
		time = time[0].split(':');
		var seconds = 0;
		for(var i = 0; i < time.length; i++){
			seconds += parseInt(time[time.length - 1 - i]) * ((i == 0) ? 1 : ((i == 1) ? 60 : 3600));
		}
		return seconds + milliSeconds; 
	};
	
	this.timestampToDate = function(time){
		time = new Date(time);
		return this.addLeadingZero(time.getDate()) + '.' + this.addLeadingZero(time.getMonth() + 1) + '.' + time.getFullYear() + ' ' + this.addLeadingZero(time.getHours()) + ':' + this.addLeadingZero(time.getMinutes());
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
	
	this.nlToBr = function(text){
		return text.replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1<br />$2');
	};
	
	this.urlToLink = function(text){
		return $('<div></div>').text(text).linkify().html();
	};
	
	this.textToHTML = function(text){
		return this.nlToBr(this.urlToLink(text));
	};
	
	this.isValidExtension = function(filename, extensions){
		var ext = filename.split('.').pop().toLowerCase();
		for(var i = 0; i < extensions.length; i++){
			if(ext == extensions[i]){
				return true;
			}
		}
		return false;
	};
	
	this.getAnnotationId = function(classes){
		var replace = ['annotation', 'imageAnnotation', 'galleryAnnotation', 'videoAnnotation', 'audioAnnotation', 'richtextAnnotation', 'pdfAnnotation', 'communityAnnotation', 'active', 'transition', 'fullscreen', ''];
		for(var i = 0; i < replace.length; i++){
			classes = classes.replace(new RegExp('sivaPlayer_' + replace[i], ''), '');
		}
		classes = classes.trim().split('_');
		classes.pop();
		return classes.join('_');
	};
	
	this.showControls = function(){
		$(this.player).addClass('sivaPlayer_hover');
	};
	
	this.hideControls = function(){
		var thisPlayer = this;
		$(this.player).removeClass('sivaPlayer_hover');
		setTimeout(function(){
			thisPlayer.hideControls();
		}, 3000);
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
				nodes += '<a href="' + this.generateNodeLink(nNode) + '" class="sivaPlayer_log_' + nNode + ((disabled) ? ' sivaPlayer_disabled' : '') + '" title="' + ((disabled) ? ((n.condition.message) ? n.condition.message[this.currentLanguage].content : this.getLabel('nodeSelectionDisabledTooltip')) : this.getLabel('nodeSelectionTooltip')) + '"><span>' + ((n.image) ? '<img src="' + this.getThumbnailURL(this.configuration.videoPath + n.image[this.currentLanguage].href) + '" />' : '') + ((n.title) ? n.title[this.currentLanguage].content : '') + '</span></a>';
			}
		}
		nodes += '</div>';
		var title = ((sceneNode.title) ? ((sceneNode.title[this.currentLanguage].content) ? sceneNode.title[this.currentLanguage].content : '') : ((this.configuration.endScene.title) ? this.configuration.endScene.title[this.currentLanguage].content :this.getLabel('endNodeTitle')));
		this.logAction('openFork', node, title);
		if(isVideoElementAvailable && !this.configuration.common.isChromecastReceiver){
			$('.sivaPlayer_topControls .sivaPlayer_prevButton, .sivaPlayer_topControls .sivaPlayer_nextButton', this.player).addClass('sivaPlayer_disabled');
			$(this.player).append('<div class="sivaPlayer_nodeSelectionSidebar"><div class="sivaPlayer_holder"><div class="sivaPlayer_title">' + title + '</div></div></div>');
			$('.sivaPlayer_nodeSelectionSidebar .sivaPlayer_holder', this.player).append(nodes);
			$('.sivaPlayer_nodeSelectionSidebar', this.player).css('width', parseInt(this.configuration.style.nodeSelectionSidebarWidth * 100) + '%')
			.show(0);
		}
		else if(!isVideoElementAvailable){
			this.createTopControls(node, false);
			this.createPopup('nodeSelection', false);
			$('.sivaPlayer_nodeSelectionPopup .sivaPlayer_title', this.player).text(title);
			$('.sivaPlayer_nodeSelectionPopup .sivaPlayer_content .sivaPlayer_scrollable', this.player).append(nodes);
			$('.sivaPlayer_nodeSelectionPopup a.sivaPlayer_current').click(function(){
				thisPlayer.closePopups(600);
			});
		}
		if(this.configuration.common.isChromecastReceiver){
				return;
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
	
	this.createSearchPopup = function(){
		var thisPlayer = this;
		this.logAction('openSearch', '', '');
		this.createPopup('search', true);
		$('.sivaPlayer_searchPopup .sivaPlayer_closeButton', this.player).click(function(){
			thisPlayer.logAction('closeSearch', '', '');
		});
		$('.sivaPlayer_searchPopup .sivaPlayer_title', this.player).append('<div class="sivaPlayer_input"><input type="text" placeholder="' + this.getLabel('searchTitle') + '" /></div>');
		$('.sivaPlayer_searchPopup .sivaPlayer_content .sivaPlayer_scrollable', this.player).append('<div class="sivaPlayer_results"></div>');
		$('.sivaPlayer_searchPopup input', this.player).focus().bind('input', function(){
			thisPlayer.logAction('searchFor', '', $(this).val());
			thisPlayer.searchFor($(this).val());
		});
	};
	
	this.createSearchSidebar = function(){
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
		$('.sivaPlayer_searchSidebar .sivaPlayer_results, .sivaPlayer_searchPopup .sivaPlayer_results', this.player).empty();
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
			$('.sivaPlayer_searchSidebar .sivaPlayer_results, .sivaPlayer_searchPopup .sivaPlayer_results', this.player).append(found[element]);
			i++;
		}
		if(i > 0){
			$('.sivaPlayer_searchSidebar a, .sivaPlayer_searchPopup a', this.player).click(function(){
				thisPlayer.logAction('selectSearchResult', $(this).attr('class').replace(/sivaPlayer_current/, '').trim().replace(/sivaPlayer_log_/, ''), $(this).text());
			});
			$('.sivaPlayer_searchSidebar .sivaPlayer_sidebarAnnotation, .sivaPlayer_searchPopup .sivaPlayer_sidebarAnnotation', this.player).click(function(){
				thisPlayer.isAnnotationSidebarVisible = true;
			});
		}
		else if(inputSufficient){
			$('.sivaPlayer_searchSidebar .sivaPlayer_results, .sivaPlayer_searchPopup .sivaPlayer_results', this.player).append('<span class="sivaPlayer_empty">' + this.getLabel('searchNoMatches') + '</span>');
		}
	};
	
	this.createSettingsPopup = function(){
		var thisPlayer = this;
		this.logAction('openSettings', '', '');
		this.createPopup('settings', true);
		$('.sivaPlayer_settingsPopup .sivaPlayer_closeButton', this.player).click(function(){
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
			content += '<a href="' + this.generateNodeLink(this.currentScene, offset) + '" class="' + ((language == this.currentLanguage) ? 'sivaPlayer_current' : '') + ' sivaPlayer_log_' + language + '" title="' + ((language == this.currentLanguage) ? this.getLabel('settingsCurrentLanguageLink') : this.getLabel('settingsChooseLanguageLink')) + '"><span>' + language + '</span></a>';
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
		$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_closeButton', this.player).click(function(){
			thisPlayer.logAction('closeTableOfContents', '', '');
		});
		$('.sivaPlayer_tableOfContentsPopup', this.player).bind('swipeleft', function(){
			thisPlayer.logAction('closeTableOfContents', '', '');
			thisPlayer.closePopups(600, false, true);
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
		var searchButton = $('<td class="sivaPlayer_button sivaPlayer_noMobileButton"><svg title="' + this.getLabel('searchTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><g class="sivaPlayer_searchOffButton"><path d="M28.431,1.458l1.53,1.833L1.53,28.991L0,27.157L28.431,1.458z"/><g><path d="M23.986,19.654c-0.498-0.497-1.397-0.449-2.303,0.021l-2.466-2.468c0.802-1.047,1.385-2.208,1.749-3.421l-8.224,7.431 c1.588-0.308,3.121-0.972,4.466-2.001l2.467,2.468c-0.471,0.903-0.518,1.804-0.021,2.302l5.657,5.655 c0.714,0.716,2.263,0.323,3.456-0.871c1.197-1.194,1.59-2.745,0.876-3.46L23.986,19.654z"/><path d="M5.532,20.089l1.792-1.632c-0.941-0.411-1.825-0.994-2.595-1.764c-3.303-3.306-3.306-8.659,0-11.963 c3.303-3.303,8.66-3.303,11.966,0c0.969,0.968,1.645,2.116,2.046,3.334l1.791-1.631c-0.521-1.196-1.267-2.318-2.245-3.297 c-4.184-4.182-10.966-4.182-15.15,0c-4.183,4.185-4.183,10.966,0,15.15C3.865,19.015,4.673,19.614,5.532,20.089z"/></g></g><g class="sivaPlayer_searchOnButton"><path d="M28.768,28.771c1.197-1.194,1.59-2.745,0.876-3.46l-5.657-5.656c-0.498-0.497-1.397-0.449-2.303,0.021 l-2.466-2.468c3.216-4.199,2.912-10.228-0.931-14.072c-4.184-4.182-10.966-4.182-15.15,0c-4.183,4.185-4.183,10.966,0,15.15 c3.843,3.842,9.872,4.146,14.072,0.931l2.467,2.468c-0.471,0.903-0.518,1.804-0.021,2.302l5.657,5.655 C26.025,30.357,27.574,29.965,28.768,28.771z M16.695,16.693c-3.308,3.303-8.663,3.303-11.966,0c-3.303-3.306-3.306-8.659,0-11.963 c3.303-3.303,8.66-3.303,11.966,0C19.998,8.032,19.998,13.387,16.695,16.693z" /></g></svg></td>')
		.click(function(){
			thisPlayer.logAction('useButton', 'search', '');
			thisPlayer.createSearchPopup();
		});
		$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_title', this.player).append(searchButton);
		var settingsButton = $('<span class="sivaPlayer_button sivaPlayer_settingsButton"><svg title="' + this.getLabel('settingsTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="30px" height="30px" viewBox="0 0 30 30" xml:space="preserve"><path d="M28.97,20.526L30,15.831l-4.81-2.11l-0.02,0.09c-0.038-0.321-0.088-0.642-0.157-0.964c-0.034-0.156-0.083-0.305-0.124-0.458 l0.193,0.301l3.782-3.644l-2.591-4.049l-4.893,1.91l0.12,0.188c-0.375-0.309-0.772-0.587-1.186-0.839l0.112,0.024l0.097-5.251 L15.83,0l-2.109,4.811l0.09,0.02c-0.321,0.038-0.642,0.088-0.964,0.157c-0.157,0.034-0.306,0.083-0.459,0.124l0.303-0.194 L9.047,1.133L4.998,3.726l1.909,4.893l0.189-0.121C6.788,8.873,6.51,9.27,6.257,9.684l0.024-0.111L1.031,9.475L0,14.17l4.811,2.109 l0.02-0.09c0.038,0.321,0.088,0.642,0.157,0.964c0.017,0.076,0.042,0.147,0.06,0.222l-3.751,3.613l2.592,4.048l4.892-1.908 c0.291,0.222,0.593,0.425,0.905,0.615l-0.112-0.024L9.474,28.97L14.169,30l2.11-4.81l-0.091-0.02 c0.321-0.038,0.643-0.087,0.965-0.157c0.075-0.017,0.146-0.042,0.221-0.06l3.613,3.751l4.049-2.591l-1.909-4.892 c0.222-0.29,0.426-0.593,0.616-0.906l-0.024,0.112L28.97,20.526z M16.039,19.832c-2.668,0.575-5.296-1.124-5.871-3.793 c-0.573-2.667,1.125-5.296,3.793-5.869c2.669-0.574,5.296,1.124,5.871,3.792C20.405,16.629,18.707,19.258,16.039,19.832z"/></svg></span>').click(function(){
			thisPlayer.logAction('useButton', 'settings', '');
			thisPlayer.createSettingsPopup();
		});
		$('.sivaPlayer_tableOfContentsPopup .sivaPlayer_title', this.player).append(settingsButton);
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
	
	this.tidyPlayer = function(delay, includesRightSidebars, oneByOne){
		var thisPlayer = this;
		if(!delay)
			delay = 800;
		var resources = '.sivaPlayer_volumeControl, .sivaPlayer_popup:not(.sivaPlayer_nodeSelectionPopup)';
		if(includesRightSidebars){
			resources += ', .sivaPlayer_searchSidebar, .sivaPlayer_annotationEditor';
		}
		var results = $(resources, this.player);
		if(oneByOne && results.length > 0){
			if(results.length == 1){
				this.restorePlayedBefore();
			}
			results = results[results.length - 1];
		}
		else{
			this.restorePlayedBefore();
		}
		if($(results).filter('.sivaPlayer_annotationEditor').length != 0 && !confirm(this.getLabel('annotationEditorLeaveMessage'))){
			return;
		}
		$(results).fadeOut(delay, function(){
			$(this).remove();
			thisPlayer.setSearchButton();
		});		
	};
	
	this.createPopup = function(id, hasCloseButton){
		var thisPlayer = this;
		var popup = $('<div class="sivaPlayer_popup sivaPlayer_' + id + 'Popup' + ((hasCloseButton) ? ' sivaPlayer_closePopup' : '') + '"><table><tr><td class="sivaPlayer_title"></td></tr><tr><td class="sivaPlayer_content"><div class="sivaPlayer_scrollable"></div></td></tr></table><span class="sivaPlayer_button sivaPlayer_closeButton"><svg title="' + this.getLabel('closeTooltip') + '" version="1.1" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" width="52.455px" height="52.455px" viewBox="0 0 52.455 52.455" xml:space="preserve"><path d="M-0.163,1l2.114-1.003L52.618,50.33l-2.114,2.128L-0.163,1z"/><path d="M50.333,0l2.122,2.122L2.122,52.455L0,50.333L50.333,0z"/></svg></span></div>');
		$('.sivaPlayer_closeButton.sivaPlayer_button', popup)
			.click(function(){
				thisPlayer.sendChromecastMessage('closePopups', [600]);
				thisPlayer.closePopups(600);
			});
		if(!hasCloseButton){
			$('.sivaPlayer_closeButton.sivaPlayer_button', popup).remove();
		}
		$(this.player).append(popup);
		this.setProportions();
	};
	
	this.closePopups = function(delay, includesRightSidebars, oneByOne){
		this.tidyPlayer(delay, includesRightSidebars, oneByOne);
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
	
	this.logAction = function(type, element, additionalInformation){
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
				'extraInfo': additionalInformation,
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
					'url': this.appendAccessToken(((this.configuration.common.logPath) ? this.configuration.common.logPath : this.configuration.videoPath) + '/log', true, true),
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
	document.addEventListener("deviceready", function(){
		for(var i = 0; i < sivaPlayerArray.length; i++){		
			sivaPlayerArray[i].onPhonegap();
		}
	}, false);
});

var sivaPlayerFragments = $.param.fragment();
$(window).bind('hashchange', function(e){
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
		sivaPlayerArray[i].onUnload();
	}
	setTimeout(function(){
		sivaPlayerClearLog();
	}, 0);
});
$(document).on('webkitfullscreenchange mozfullscreenchange fullscreenchange', function(){
	for(var i = 0; i < sivaPlayerArray.length; i++){
		sivaPlayerArray[i].setFullscreenButton();
	}
});
window['__onGCastApiAvailable'] = function(loaded, errorInfo) {
	if(loaded){
		for(var i = 0; i < sivaPlayerArray.length; i++){		
			sivaPlayerArray[i].onChromecastAvailable();
		}
	}
	else{
		console.log(errorInfo);
	}
};