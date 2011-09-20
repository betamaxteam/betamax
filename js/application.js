$(document).ready(function() {

    // Google code prettify
    // ====================

    $('pre').addClass('prettyprint');
    prettyPrint();

    // scroll spy logic
    // ================

    var activeTarget,
            position = {},
            $window = $(window),
            nav = $('body > .topbar li a'),
            targets = nav.map(function () {
                return $(this).attr('href');
            }),
            offsets = $.map(targets, function (id) {
                return $(id).offset().top;
            });


    function setButton(id) {
        nav.parent("li").removeClass('active');
        $(nav[$.inArray(id, targets)]).parent("li").addClass('active');
    }

    function processScroll(e) {
        var scrollTop = $window.scrollTop() + 10, i;
        for (i = offsets.length; i--;) {
            if (activeTarget != targets[i] && scrollTop >= offsets[i] && (!offsets[i + 1] || scrollTop <= offsets[i + 1])) {
                activeTarget = targets[i];
                setButton(activeTarget);
            }
        }
    }

    nav.click(function () {
        processScroll();
    });

    processScroll();

    $window.scroll(processScroll);

    // set up pill containers

    // wrap a container around each section we want to be a tab
    $('#maven, #gradle, #grails, #junit, #spock').each(function() {
        var tab = $('<div></div>', {
            id: $(this).attr('id') + '-tab'
        });
        $(this).nextUntil('h1,h2,h3').andSelf().wrapAll(tab);
        $(this).hide();
    });

    // wrap containers around each tab group
    $('#maven-tab, #gradle-tab, #grails-tab').wrapAll('<div id="installation-tab-content" class="pill-content"></div>');
    $('#junit-tab, #spock-tab').wrapAll('<div id="example-tab-content" class="pill-content"></div>');

    // for each tab container, create nav links
    $('.pill-content').each(function() {
        var tabs = $('<ul class="pills"></ul>');
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
    $('.pill-content > :first-child, .pills > :first-child').addClass('active');
    $('.pills').tabs();

});