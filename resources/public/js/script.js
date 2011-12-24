lazypress = {};

lazypress.publish = function() {
  if($('content').get('value') == '') {
    lazypress.roar.alert('Ops!', 'Write something, please.');
    return;
  }

  $('publish-button').removeEvents('click');
  $('publish-button').set('text', 'Publishing ...');
  if ($('author').get('value') != '') {
    if (!$('author').checkValidity()) {
      lazypress.roar.alert('Invalid User ID',
                          'Make your ID with 0-9 a-z A-Z - _');
      $('publish-button').set('text', 'Publish');
      $('publish-button').addEvent('click', lazypress.publish);
      return;
    }
    var author = $('author').get('value');
    var passwd = $('password').get('value');
    var req = new Request.JSON(
      {url: '/login',
       onSuccess: function(r,_){
         if (r.result == 'ok') {
           lazypress._publish();
         } else {
           lazypress.roar.alert('Login Failed.',
             'Your ID is captured. Or the password you typed is invalid.');
           $('publish-button').set('text', 'Publish');
           $('publish-button').addEvent('click', lazypress.publish);
         }
         
       }}
    );
    req.post({'author': author, 'password': passwd});
  } else {
    lazypress._publish();
  }
};

lazypress._publish = function() {

  var content = $('content').get('value');
  var title = $('title').get('value');
  var author = $('author').get('value');
  var req = new Request.JSON(
    {url: "/save", 
     onSuccess: function(r,_){
       if (r.result == 'ok'){
         var id = r.id;
         window.location = "/p/"+id;
       } else {
         lazypress.roar.alert('Failed', 'Unknown error');
       }
     }});
  req.post({'content': content, 'title': title, 'author': author});
};

lazypress.preview = function() {
  $('preview-button').removeEvents('click');
  $('preview-button').set('text', 'Rendering ...');
  var content = $('content').get('value');
  var req = new Request.HTML(
    {url: "preview",
     onSuccess: function(_,r,_,_){
       $('preview-box').empty();
       r.inject($('preview-box'));
       $('content-box').toggleClass('hidden');
       $('preview-box').toggleClass('hidden');
       $('preview-button').set('text', 'Edit');
       $('preview-button').addEvent('click', lazypress.edit);
     }});
  req.post({'content': content});
};

lazypress.edit = function(e) {
  $('preview-box').toggleClass('hidden');
  $('content-box').toggleClass('hidden');
  $('preview-button').set('text', 'Preview');
  $('preview-button').removeEvents(['click']);
  $('preview-button').addEvent('click', lazypress.preview);

};

lazypress.init = function( ) {
  $('preview-button').addEvent('click', lazypress.preview);
  $('publish-button').addEvent('click', lazypress.publish);
  lazypress.roar = new Roar({duration: 5000});
};

