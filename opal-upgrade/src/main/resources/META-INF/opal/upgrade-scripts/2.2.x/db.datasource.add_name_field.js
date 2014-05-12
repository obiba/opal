// mongeez formatted javascript
// changeset obiba:add_name_field
db.datasource.find({'name' : { '$exists' : false }}).forEach(function(doc){
    doc.name = doc._id;
    db.datasource.save(doc);
});