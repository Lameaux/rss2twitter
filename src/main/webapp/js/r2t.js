var urlPattern = /http:\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/;

function loadRssFeeds() {

	$.ajax({
		type : 'GET',
		url : '/feeds',
		dataType: 'json'
	}).done(function(feeds){
		$('#rss_list').html('');
		for (var i = 0; i < feeds.length; i++) {
			$('#rss_list').append('<p id="feed_'+ feeds[i].id +'">' + feeds[i].url + '</p>');
		}
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
				$('#add_rss_error').html('Invalid URL');
				return;
			}
			
			$.ajax({
				type : 'POST',
				url : '/feeds/new',
				data : {
					url : rssUrl
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