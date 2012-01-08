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
         if (lazypress._backup_thread) {
           clearInterval(lazypress._backup_thread);
           delete localStorage.lparticle;
         }
         window.onbeforeunload = null;
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
                             'This article has been deleted permanently.');
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
             $('user').set('href', "/a/"+r.id);
             $('login').addClass('hidden');
             $('login').removeClass('inline');
             $('logout').addClass('inline');
             $('logout').removeClass('hidden');
             lazypress.roar.alert('Login success.', 'Welcome to lazypress, '+r.id);
             if ($('title')) {
               lazypress.try_recovery();
             }
           } else if (r.result == 'id-required') {
               lazypress.ask_for_id(false);
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

  lazypress.roar = new Roar({duration: 5000});
};

lazypress.try_recovery = function() {
  if (localStorage) {
    if (localStorage.lparticle) {
      var article = JSON.parse(localStorage.lparticle);
      var author = article.author;
      if (author == $('user').get('text') &&
          confirm('Unpublished article detected, restore it ?')) {
        $('title').set('value', article.title);
        $('content').set('value',article.content);
      } else {
        delete localStorage.lparticle;
      }
    }
  }
};

lazypress.start_backup = function() {
  var backup_function = function() {
    var author = $('user').get('text');
    if (author.length>0){
      var title = $('title').get('value');
      var content = $('content').get('value');
        
      if (title.length >0 || content.length>0) {
        var article = {title: title, 
                       content: content,
                       author: author};
        localStorage.lparticle = JSON.stringify(article);
      }
      
    }
  };
  if (localStorage){
    lazypress._backup_thread = backup_function.periodical(15000);
  }

};

lazypress.ask_for_id = function(alt_msg) {
  var msg = alt_msg || "Welcome to LazyPress! <br/>Get yourself a unique ID: [A-Za-z0-9_]";
  new MooDialog.Prompt(msg, function(ret){
    if (!ret.test('^[A-Za-z0-9_]+$')) {
      lazypress.ask_for_id("Invalid characters found in your ID.<br/> Only [A-Za-z0-9_] are allowed:");
      return ;
    }
    var req = new Request.JSON({
      url: "/save-id",
      onSuccess: function (r, _) {
        if (r.result == "ok") {
          $('user').set('text', r.id);
          $('user').set('href', "/a/"+r.id);
          $('login').addClass('hidden');
          $('login').removeClass('inline');
          $('logout').addClass('inline');
          $('logout').removeClass('hidden');
          lazypress.roar.alert('Login success.', 
                               'Welcome to lazypress, '+r.id);
        } else {
          lazypress.ask_for_id("Sorry, the ID you gave us is captured.<br/> Find a new ID: [A-Za-z0-9_]");
        }
      }
    });
    req.post({'uid':ret});
  }, {'closeButton': false, 'useEscKey': false});
};


