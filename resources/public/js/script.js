lazypress = {};

lazypress.publish = function() {
  $$('#publish-button').removeEvents(['click']);
  $$('#publish-button').set('text', 'Publishing ...');
  var content = $$('#content').get('value')[0];
  var req = new Request.JSON(
    {url: "/save", 
     onSuccess: function(r,_){
       var id = r.id;
       window.location = "/v/"+id;
     }})
    .post({'content': content});
};
