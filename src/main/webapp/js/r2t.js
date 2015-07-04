var urlPattern = /http:\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/;
var numberPattern = /[0-9]+/;

function deleteFeed(feed_id) {
	$.ajax({
		type : 'POST',
		url : '/feeds/' + feed_id + '/delete'
	}).done(function(){
		$('tr#feed-' + feed_id).html('');
	}).fail(function(){
		$('#add_rss_error').removeClass('hidden');
		$('#add_rss_error').html('Unknown Server Error');
	});	
}

function makeFeedText(feed_id) {
	var tr = $('tr#feed-' + feed_id);
	tr.removeClass('warning');	

	var tr_feed_url_input = tr.find('.input-feed-url');
	var feedUrl = tr_feed_url_input.val();
	tr_feed_url_input.replaceWith(feedUrl);

	var tr_feed_frequency_input = tr.find('.input-feed-frequency');
	var feedFrequency = tr_feed_frequency_input.val();
	tr_feed_frequency_input.replaceWith(feedFrequency);
	
	var tr_feed_action = tr.find('.feed-action');
	tr_feed_action.html('');
	tr_feed_action.append(
			'<button class="feed-edit btn btn-primary btn-xs"><span class="glyphicon glyphicon-edit"></span> Edit</button> ' +
			'<button class="feed-delete btn btn-danger btn-xs"><span class="glyphicon glyphicon-remove"></span> Delete</button>'
	);	

}

function makeFeedEditable(feed_id) {
	var tr = $('tr#feed-' + feed_id);
	tr.addClass('warning');
	
	var tr_feed_url = tr.find('.feed-url');
	var feedUrl = tr_feed_url.html();
	tr_feed_url.html('<input id="feed_url_' + feed_id + '" class="input-feed-url" type="text" value="' + feedUrl + '" />');

	var tr_feed_frequency = tr.find('.feed-frequency span');
	var feedFrequency = tr_feed_frequency.html();
	tr_feed_frequency.html('<input id="feed_frequency_' + feed_id + '" class="input-feed-frequency" type="text" value="' + feedFrequency + '" />');

	var tr_feed_action = tr.find('.feed-action');
	tr_feed_action.html('');
	tr_feed_action.append(
			'<button class="feed-save btn btn-success btn-xs"><span class="glyphicon glyphicon-ok"></span> Save</button> ' + 	
			'<button class="feed-cancel btn btn-primary btn-xs"><span class="glyphicon glyphicon-erase"></span> Cancel</button>'
	);	

	tr.find('button.feed-save').click(function(){
		makeFeedText(feed_id);
	});	
	
	tr.find('button.feed-cancel').click(function(){
		makeFeedText(feed_id);
	});
	
}

function loadRssFeeds() {

	$.ajax({
		type : 'GET',
		url : '/feeds',
		dataType: 'json'
	}).done(function(feeds){
		$('#rss_list').html('');
		var feed_list = '<table class="table table-striped">';
		feed_list = feed_list + '<tr>';
		feed_list = feed_list + '<th>Feed URL</th>';
		feed_list = feed_list + '<th>Update Interval</th>';
		feed_list = feed_list + '<th>Status</th>';
		feed_list = feed_list + '<th>Last Update</th>';
		feed_list = feed_list + '<th>Action</th>';
		feed_list = feed_list + '</tr>';
		
		for (var i = 0; i < feeds.length; i++) {
			var feed_tr = '';
			feed_tr = feed_tr + '<tr id="feed-'+ feeds[i].id +'">';
			feed_tr = feed_tr + '<td class="feed-url">' + feeds[i].url + '</td>';
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
				feed_tr = feed_tr + '<span class="label label-danger">ERROR</span>';			
			}
			feed_tr = feed_tr + '</td>';
			
			var updated_iso = new Date( feeds[i].updated ).toISOString();
			var updated_text = updated_iso.replace('T', ' ').replace(/.[0-9]{3}Z/, '');
			feed_tr = feed_tr + '<td class="feed-updated">' + updated_text + '</td>';
			feed_tr = feed_tr + '<td class="feed-action">';			
			feed_tr = feed_tr + '<button class="feed-edit btn btn-primary btn-xs"><span class="glyphicon glyphicon-edit"></span> Edit</button> ';
			feed_tr = feed_tr + '<button class="feed-delete btn btn-danger btn-xs"><span class="glyphicon glyphicon-remove"></span> Delete</button>';
			feed_tr = feed_tr + '</td>';
			feed_tr = feed_tr + '<tr>';
			
			feed_list = feed_list + feed_tr;
		}
		
		feed_list = feed_list + '</table>';		
		$('#rss_list').append(feed_list);		
		
		$('.feed-delete').click(function(){
			var feed_id = $(this).parent().parent().attr('id').split("-")[1];
			$('tr#feed-' + feed_id).addClass('warning');
			if (!confirm('Delete feed?')) {
				$('tr#feed-' + feed_id).removeClass('warning');
				return;
			}			
			deleteFeed(feed_id);
		});

		$('.feed-edit').click(function(){
			var feed_id = $(this).parent().parent().attr('id').split("-")[1];
			makeFeedEditable(feed_id);
		});		
		
	}).fail(function(){
		$('#rss_list').html('Error loading feeds');
	});	
	
}

!function($) {

	$(function() {
		
		$('button#add_rss_url').click(function(){
			var rssUrl = $('input#rss_url').val();
			if (!urlPattern.test(rssUrl)) {
				$('#add_rss_error').removeClass('hidden');
				$('#add_rss_error').html('Invalid RSS feed URL');
				return;
			}

			var rssFrequency = $('input#rss_frequency').val();			
			if (!numberPattern.test(rssFrequency) || parseInt(rssFrequency) < 1) {
				$('#add_rss_error').removeClass('hidden');
				$('#add_rss_error').html('Update frequency should be at least 1 hour');
				return;
			}			
			
			$.ajax({
				type : 'POST',
				url : '/feeds/new',
				data : {
					url : rssUrl,
					frequency : rssFrequency
				}
			}).done(function(){
				$('#add_rss_error').addClass('hidden');
				$('#add_rss_error').html('');
				$('input#rss_url').val('http://');
				loadRssFeeds();
			}).fail(function(){
				$('#add_rss_error').removeClass('hidden');
				$('#add_rss_error').html('Unknown Server Error');
			});
			
		});
		
		// init
		
		loadRssFeeds();
		
	});

}(window.jQuery);