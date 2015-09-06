var urlPattern = /https?:\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/;

function vkRestoreActionButtons(feed_object) {
	feed_object.removeClass('info');
	
	var tr_feed_action = feed_object.find('.feed-action');
	tr_feed_action.html('');
	tr_feed_action.append(
			'<button class="feed-edit btn btn-primary btn-xs"><span class="glyphicon glyphicon-edit"></span> Edit</button> ' +
			'<button class="feed-delete btn btn-danger btn-xs"><span class="glyphicon glyphicon-remove"></span> Delete</button>'
	);
	
	feed_object.find('button.feed-edit').click(function(){
		vkMakeFeedEditable(feed_object);
	});	
	
	feed_object.find('button.feed-delete').click(function(){
		vkMakeFeedDeletable(feed_object);
	});	
}

function vkMakeFeedText(feed_object) {

	var tr_feed_url_input = feed_object.find('.input-feed-url');
	var feedUrl = tr_feed_url_input.val();
	tr_feed_url_input.replaceWith(feedUrl);

	var tr_feed_frequency = feed_object.find('.feed-frequency');	
	var tr_feed_frequency_input = tr_feed_frequency.find('.select-feed-frequency');
	var feedFrequency = tr_feed_frequency_input.val();
	
	tr_feed_frequency_input.replaceWith('<span class="hours">' + feedFrequency + '</span>');
	tr_feed_frequency.append(' ' + (feedFrequency == '1' ? 'hour' : 'hours'));
	
	vkRestoreActionButtons(feed_object);
}

function vkMakeFeedDeletable(feed_object) {
	feed_object.addClass('info');

	var feed_id = feed_object.attr('id').split('-')[1];	
	
	var tr_feed_action = feed_object.find('.feed-action');
	tr_feed_action.html('');
	tr_feed_action.append(
			'<button class="feed-confirm-delete btn btn-danger btn-xs"><span class="glyphicon glyphicon-ok"></span> Confirm delete</button> ' + 	
			'<button class="feed-cancel btn btn-success btn-xs"><span class="glyphicon glyphicon-repeat"></span> Cancel</button>'
	);	
	feed_object.find('button.feed-confirm-delete').click(function(){
		$.ajax({
			type : 'POST',
			url : '/vk/feeds/' + feed_id + '/delete'
		}).done(function(){
			feed_object.html('');
		}).fail(function(){
			$('#add_rss_error').removeClass('hidden');
			$('#add_rss_error').html('Unknown Server Error');
			vkRestoreActionButtons(feed_object);
		});			
	});	
	
	feed_object.find('button.feed-cancel').click(function(){
		vkRestoreActionButtons(feed_object);
	});	
	
}	
	
function vkMakeFeedEditable(feed_object) {
	feed_object.addClass('info');
	
	var feed_id = feed_object.attr('id').split('-')[1];
	
	var tr_feed_url = feed_object.find('.feed-url');
	var feedUrl = tr_feed_url.html();
	tr_feed_url.html('<input id="feed_url_' + feed_id + '" class="input-feed-url" type="text" value="' + feedUrl + '" />');

	var tr_feed_frequency = feed_object.find('.feed-frequency');
	var tr_feed_frequency_span = tr_feed_frequency.find('span');
	var feedFrequency = tr_feed_frequency_span.html();
	var frequency_select = '';
	frequency_select = frequency_select + '<select id="feed_frequency_' + feed_id + '" class="select-feed-frequency">';
	frequency_select = frequency_select + '<option value="24" ' + (feedFrequency == '24' ? 'selected':'') + '>24 hours</option>';
	frequency_select = frequency_select + '<option value="12" ' + (feedFrequency == '12' ? 'selected':'') + '>12 hours</option>';
	frequency_select = frequency_select + '<option value="6" ' + (feedFrequency == '6' ? 'selected':'') + '>6 hours</option>';
	frequency_select = frequency_select + '<option value="3" ' + (feedFrequency == '3' ? 'selected':'') + '>3 hours</option>';
	frequency_select = frequency_select + '<option value="2" ' + (feedFrequency == '2' ? 'selected':'') + '>2 hours</option>';
	frequency_select = frequency_select + '<option value="1" ' + (feedFrequency == '1' ? 'selected':'') + '>1 hour</option>';
	frequency_select = frequency_select + '</select>';
	
	tr_feed_frequency.html(frequency_select);

	var tr_feed_action = feed_object.find('.feed-action');
	tr_feed_action.html('');
	tr_feed_action.append(
			'<button class="feed-save btn btn-primary btn-xs"><span class="glyphicon glyphicon-ok"></span> Save</button> ' + 	
			'<button class="feed-cancel btn btn-success btn-xs"><span class="glyphicon glyphicon-repeat"></span> Cancel</button>'
	);	

	feed_object.find('button.feed-save').click(function(){
		var feedUrlNew = feed_object.find('.input-feed-url').val();
		var feedFrequencyNew = feed_object.find('.select-feed-frequency').val();
		$.ajax({
			type : 'POST',
			url : '/vk/feeds/' + feed_id + '/edit',
			data : {
				url : feedUrlNew,
				frequency : feedFrequencyNew
			}			
		}).done(function(status){
			if (status == '0') {
				feed_object.find('.feed-status').html('<span class="label label-primary">NEW</span>');
				feed_object.find('.feed-updated').html('');
			}
			vkMakeFeedText(feed_object);
		}).fail(function(){
			$('#add_rss_error').removeClass('hidden');
			$('#add_rss_error').html('Unknown Server Error');
			vkRestoreActionButtons(feed_object);
		});			
	});	
	
	feed_object.find('button.feed-cancel').click(function(){
		feed_object.find('.input-feed-url').val(feedUrl);
		feed_object.find('.select-feed-frequency').val(feedFrequency);
		vkMakeFeedText(feed_object);
	});
	
}

function vkLoadRssFeeds() {

	$.ajax({
		type : 'GET',
		url : '/vk/feeds',
		dataType: 'json'
	}).done(function(feeds){
		$('#rss_list').html('');
		for (var i = 0; i < feeds.length; i++) {
			var feed_tr = '';
			feed_tr = feed_tr + '<tr id="feed-'+ feeds[i].id +'">';
			feed_tr = feed_tr + '<td class="feed-url">' + feeds[i].url + '</td>';
			feed_tr = feed_tr + '<td class="feed-wall">' + getGroupNameById(feeds[i].wallOwnerId) + ' ' + feeds[i].wallOwnerId +'</td>';
			feed_tr = feed_tr + '<td class="feed-frequency">';
			var freq = feeds[i].frequency;
			feed_tr = feed_tr + '<span class="hours">' + freq + '</span> ' + (freq > 1 ? 'hours' : 'hour');
			feed_tr = feed_tr + '</td>';			
			feed_tr = feed_tr + '<td class="feed-status">';
			if (feeds[i].status == 0) {
				feed_tr = feed_tr + '<span class="label label-primary">NEW</span>';
			} else if (feeds[i].status == 1) {
				feed_tr = feed_tr + '<span class="label label-success">OK</span>';			
			} else if (feeds[i].status == 2) {
				feed_tr = feed_tr + '<span class="label label-danger">' + feeds[i].errorText + '</span>';			
			}
			feed_tr = feed_tr + '</td>';
			var updated_text = '';
			if (feeds[i].updated > 0) {
				var updated_iso = new Date( feeds[i].updated ).toISOString();
				updated_text = updated_iso.replace('T', ' ').replace(/.[0-9]{3}Z/, '');
			}
			feed_tr = feed_tr + '<td class="feed-updated">' + updated_text + '</td>';
			feed_tr = feed_tr + '<td class="feed-action">';			
			feed_tr = feed_tr + '<button class="feed-edit btn btn-primary btn-xs"><span class="glyphicon glyphicon-edit"></span> Edit</button> ';
			feed_tr = feed_tr + '<button class="feed-delete btn btn-danger btn-xs"><span class="glyphicon glyphicon-remove"></span> Delete</button>';
			feed_tr = feed_tr + '</td>';
			feed_tr = feed_tr + '<tr>';
			
			var feed_object = $(feed_tr);
			feed_object.find('.feed-delete').click(function(){
				vkMakeFeedDeletable($(this).parent().parent());	
			});
			feed_object.find('.feed-edit').click(function(){
				vkMakeFeedEditable($(this).parent().parent());
			});				
			$('#rss_list').append(feed_object);
		}
	}).fail(function(){
		$('#add_rss_error').removeClass('hidden');		
		$('#add_rss_error').html('Error loading feeds');
	});	
	
}

function vkSaveNewFeed(feed_object) {
	var url_input = feed_object.find('.input-feed-url');
	if (!urlPattern.test(url_input.val())) {
		url_input.data('title', 'Invalid RSS feed URL');
		url_input.tooltip('show');
		return;
	}
	var rssUrl = url_input.val();
	var rssFrequency = feed_object.find('.select-feed-frequency').val();			
	var rssWall = feed_object.find('.select-feed-wall').val();
	$.ajax({
		type : 'POST',
		url : '/vk/feeds/new',
		data : {
			url : rssUrl,
			frequency : rssFrequency,
			wall: rssWall
		}
	}).done(function(id){
		if (id == '0') {
			url_input.data('title', 'Duplicate RSS feed URL');
			url_input.tooltip('show');			
		} else {
			feed_object.attr('id', 'feed-' + id);
			vkMakeFeedText(feed_object);
		}
	}).fail(function(){
		$('#add_rss_error').removeClass('hidden');
		$('#add_rss_error').html('Unknown Server Error');
	});	
}

function vkLoadGroups() {

	$.ajax({
		type : 'GET',
		url : '/vk/groups',
		dataType: 'json'
	}).done(function(response){
		groups = response;
	}).fail(function(){
		$('#add_rss_error').removeClass('hidden');		
		$('#add_rss_error').html('Error loading groups');
	});	
	
}

!function($) {

	$(function() {
		
		$.ajaxSetup({ cache: false });
		
		$('button#vk_add_rss_url').click(function() {
			var feed_tr = '';
			feed_tr = feed_tr + '<tr class="info">';
			feed_tr = feed_tr + '<td class="feed-url">';
			feed_tr = feed_tr + '<input class="input-feed-url" type="text" value="http://" />';
			feed_tr = feed_tr + '</td>';
			feed_tr = feed_tr + '<td class="feed-wall">';
			feed_tr = feed_tr + '<input class="input-feed-wall" type="text" value="" />';			
			feed_tr = feed_tr + '</td>';
			feed_tr = feed_tr + '<td class="feed-frequency">';

			var frequency_select = '';
			frequency_select = frequency_select + '<select class="select-feed-frequency">';
			frequency_select = frequency_select + '<option value="24">24 hours</option>';
			frequency_select = frequency_select + '<option value="12">12 hours</option>';
			frequency_select = frequency_select + '<option value="6">6 hours</option>';
			frequency_select = frequency_select + '<option value="3">3 hours</option>';
			frequency_select = frequency_select + '<option value="2">2 hours</option>';
			frequency_select = frequency_select + '<option value="1">1 hour</option>';
			frequency_select = frequency_select + '</select>';			
			feed_tr = feed_tr + frequency_select;
			
			feed_tr = feed_tr + '</td>';			
			feed_tr = feed_tr + '<td class="feed-status">';
			feed_tr = feed_tr + '<span class="label label-primary">NEW</span>';
			feed_tr = feed_tr + '</td>';
			
			feed_tr = feed_tr + '<td class="feed-updated"></td>';
			feed_tr = feed_tr + '<td class="feed-action">';		
			feed_tr = feed_tr + '<button class="feed-save btn btn-primary btn-xs"><span class="glyphicon glyphicon-ok"></span> Save</button> '; 	
			feed_tr = feed_tr + '<button class="feed-cancel btn btn-success btn-xs"><span class="glyphicon glyphicon-repeat"></span> Cancel</button>';
			feed_tr = feed_tr + '</td>';
			feed_tr = feed_tr + '<tr>';

			var feed_object = $(feed_tr);
			feed_object.find(".feed-save").click(function(){
				vkSaveNewFeed($(this).parent().parent());
			});

			feed_object.find(".feed-cancel").click(function(){
				$(this).parent().parent().html('');
			});
			$('#rss_list').append(feed_object);
		});
		
		// init
		if ($('#vk_add_rss_url').length) {
			vkLoadGroups();
			vkLoadRssFeeds();
		}
		
		if ($('a#vk-callback-button').length) {
			$('a#vk-callback-button').click(function(){
				if ($('input#vk-callback-url').val()) {
					var callbackUrl = $('input#vk-callback-url').val();
					var callbackUrlParts = callbackUrl.split("#");
					window.location.href = "/vk/oauth?" + callbackUrlParts[1];
				}
			});
		}
		
		
	});

}(window.jQuery);