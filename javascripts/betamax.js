$(document).ready(function() {

    // Google code prettify

    $('pre').addClass('prettyprint');
    prettyPrint();

	// fix nav when page is scrolled down past header
	var nav = $('nav');
	var navPos = nav.parent('.sidebar').offset().top;
	$(window).scroll(function(event) {
		nav.toggleClass('fix', $(window).scrollTop() > navPos);
	});

    // wrap a container around each section we want to be a tab
    $('#maven, #gradle, #grails, #junit, #spock').each(function() {
        var tab = $('<li></li>', {
            id: $(this).attr('id') + '-tab'
        });
        $(this).nextUntil('h1,h2,h3').andSelf().wrapAll(tab);
        $(this).hide();
    });

    // wrap containers around each tab group
    $('#maven-tab, #gradle-tab, #grails-tab').wrapAll('<ul class="tabs-content"></ul>');
    $('#junit-tab, #spock-tab').wrapAll('<ul class="tabs-content"></ul>');

    // for each tab container, create nav links
    $('.tabs-content').each(function() {
        var tabs = $('<ul class="tabs"></ul>');
        $(this).children().each(function() {
            var tab = $('<li></li>');
            tab.append($('<a></a>', {
                href: '#' + $(this).attr('id'),
                text: $(this).find('h3').text()
            }));
            tabs.append(tab);
        });
        tabs.insertBefore(this);
    });

    // activate the first link & tab in each group
    $('.tabs-content li:first-child, .tabs li:first-child a').addClass('active');

	// replace h1 with fancier but less SEO-compliant text
	$('h1, nav h3').html('&szlig;etamax');
	
	// icons for personal links
	$('#authors').next('p').find('a').each(function() {
		$(this).addClass('icon').addClass($(this).text());
	});
	
	// FOUC prevention
	$(window).load(function() {
		$('body').addClass('ready');
	});

});