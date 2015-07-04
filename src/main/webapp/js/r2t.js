var urlPattern = /http:\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/;

function deleteFeed(feed_id) {
	
	if (!confirm('Delete feed?')) {
		return;
	}
	
	$.ajax({
		type : 'POST',
		url : '/feeds/' + feed_id + '/delete'
	}).done(function(){
		loadRssFeeds();
	}).fail(function(){
		$('#add_rss_error').removeClass('hidden');
		$('#add_rss_error').html('Unknown Server Error');
	});	
}

function loadRssFeeds() {

	$.ajax({
		type : 'GET',
		url : '/feeds',
		dataType: 'json'
	}).done(function(feeds){
		$('#rss_list').html('');
		for (var i = 0; i < feeds.length; i++) {
			var feed_p = '';
			feed_p = feed_p + '<p id="feed-'+ feeds[i].id +'">';
			feed_p = feed_p + '<span class="feed-url">' + feeds[i].url + '</span>';
			feed_p = feed_p + '<span class="feed-frequency">' + feeds[i].frequency + '</span>';
			feed_p = feed_p + '<button class="feed-edit"><span class="glyphicon glyphicon-edit"></span></button>';
			feed_p = feed_p + '<button class="feed-delete"><span class="glyphicon glyphicon-remove"></span></button>';
			feed_p = feed_p + '</p>';
			$('#rss_list').append(feed_p);
		}
		
		$('.feed-delete').click(function(){
			var feed_id = $(this).parent().attr('id').split("-")[1];
			deleteFeed(feed_id);
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
			if (!$.isNumeric(rssFrequency) || parseInt(rssFrequency) < 1) {
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