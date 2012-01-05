lazypress = {};

lazypress.publish = function() {
  var content = $('content').get('value');
  var title = $('title').get('value');
  var data = {'content':content, 'title':title};
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
  window.location = "/e/" + $('id').get('value');
};

lazypress.delete = function() {
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

lazypress.login = function() {
  navigator.id.getVerifiedEmail(function(assertion) {
    if (assertion) {
      var req = new Request.JSON(
        {url: '/login',
         onSuccess: function(r,_){
           if(r.result == 'ok') {
             $('user').set('text', r.id);
             $('login').addClass('hidden');
             $('login').removeClass('inline');
             $('logout').addClass('inline');
             $('logout').removeClass('hidden');
             lazypress.roar.alert('Login success.', 'Welcome to lazypress, '+r.id);
           } else {
             lazypress.roar.alert('Login failed.', 'Do you mind to try again ?');
           }
         }}
      );
      req.post({'assertion': assertion});
    } else {
      lazypress.roar.alert('Login failed.', 'Seems you have canceled the login process.');
    }
  });
};

lazypress.logout = function() {
  var req = new Request.JSON(
    {url: '/logout',
     onSuccess: function(r,_) {
       window.location = "/";
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
    $('delete-button').addEvent('click', function(){
      if (window.confirm("Sure to delete this article?")){
        lazypress.delete();
      }
    });
  }
  if ($('user')) {
    $('user').addEvent('click', function(){
      if(window.confirm("Logout ?")){
        lazypress.logout();
      }
    })
  }

  lazypress.roar = new Roar({duration: 5000});
};

