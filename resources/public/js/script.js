lazypress = {};

lazypress.publish = function() {
  if($('content').get('value') == '') {
    lazypress.roar.alert('Ops!', 'Write something, please.');
    return;
  }

  $('publish-button').removeEvents('click');
  $('publish-button').set('text', 'Publishing ...');
  if ($('password') && $('author').get('value') != '') {
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
  var data = {'content':content, 'title':title, 'author':author};
  if ($('id')) {
    data['id'] = $('id').get('value');
  }
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
  req.post(data);
};

lazypress.preview = function() {
  $('preview-button').removeEvents('click');
  $('preview-button').set('text', 'Rendering ...');
  var content = $('content').get('value');
  var req = new Request.HTML(
    {url: "/preview",
     onSuccess: function(_,_,r,_){
       $('preview-box').set('html', r);
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

lazypress.pedit = function() {
  $('edit-button').removeEvents('click');
  $('edit-button').set('text', 'Loading ...');
  var author = $('author').get('value');
  var passwd = $('password').get('value');
  var req = new Request.JSON(
    {url: '/login',
     onSuccess: function(r,_){
       if (r.result == 'ok') {
         window.location = "/e/" + $('id').get('value');
       } else {
         lazypress.roar.alert('Login Failed.',
           'Your ID is captured. Or the password you typed is invalid.');
         $('edit-button').set('text', 'Edit');
         $('edit-button').addEvent('click', lazypress.pedit);
       }
       
     }}
  );
  req.post({'author': author, 'password': passwd});
};

lazypress.delete = function() {
  $('delete-button').removeEvents('click');
  $('delete-button').set('text', 'Loading ...');
  var author = $('author').get('value');
  var passwd = $('password').get('value');
  var req = new Request.JSON(
    {url: '/login',
     onSuccess: function(r,_){
       if (r.result == 'ok') {
         lazypress._delete();
       } else {
         lazypress.roar.alert('Login Failed.',
           'Your ID is captured. Or the password you typed is invalid.');
         $('delete-button').set('text', 'Delete');
         $('delete-button').addEvent('click', lazypress.delete);
       }
       
     }}
  );
  req.post({'author': author, 'password': passwd});
};

lazypress._delete = function() {
  var id = $('id').get('value');
  var req = new Request.JSON(
    {url: '/d/'+id,
     onSuccess: function(r,_) {
       if(r.result == 'ok'){
         lazypress.roar.alert('Article Deleted',
                             'This article has been deleted permanently.')
         $('delete-button').set('text', 'Deleted');
       } else {
         lazypress.roar.alert('Unknown error.', 'Article not removed.');
       }
     }}
  );
  req.post();
};

lazypress.init = function( ) {
  if ($('preview-button')) {
    $('preview-button').addEvent('click', lazypress.preview);
  }
  if ($('publish-button')) {
    $('publish-button').addEvent('click', lazypress.publish);
  }
  if ($('edit-button')) {
    $('edit-button').addEvent('click', lazypress.pedit);
  }
  if ($('delete-button')) {
    $('delete-button').addEvent('click', lazypress.delete);
  }

  lazypress.roar = new Roar({duration: 5000});
};

