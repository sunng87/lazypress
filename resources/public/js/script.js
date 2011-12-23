lazypress = {};

lazypress.publish = function() {
  $('publish-button').removeEvents(['click']);
  $('publish-button').set('text', 'Publishing ...');
  var content = $('content').get('value');
  var req = new Request.JSON(
    {url: "/save", 
     onSuccess: function(r,_){
       var id = r.id;
       window.location = "/v/"+id;
     }});
  req.post({'content': content});
};

lazypress.preview = function() {
  $('preview-button').removeEvents(['click']);
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

lazypress.edit = function() {
  $('preview-box').toggleClass('hidden');
  $('content-box').toggleClass('hidden');
  $('preview-button').removeEvent('click', lazypress.edit);
  $('preview-button').addEvent('click', lazypress.preview);
  $('preview-button').set('text', 'Preview');
};
