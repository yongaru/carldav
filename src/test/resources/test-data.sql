INSERT INTO users(id,locked,email,password,role) VALUES (1,0,'root@localhost','098f6bcd4621d373cade4e832627b4f6','ROLE_ADMIN');
INSERT INTO users(id,locked,email,password,role) VALUES (2,0,'test01@localhost.de','098f6bcd4621d373cade4e832627b4f6','ROLE_USER');
INSERT INTO users(id,locked,email,password,role) VALUES (3,0,'test02@localhost.de','098f6bcd4621d373cade4e832627b4f6','ROLE_USER');

INSERT INTO item (collectionid, itemtype, id, etag, modifydate, clientcreatedate, clientmodifieddate, displayname, itemname, uid,  icaluid, contentEncoding, contentLanguage, contentType, hasmodifications, ownerid, modifiesitemid)
VALUES (null, 'homecollection', 1, 'ghFexXxxU+9KC/of1jmJ82wMFig=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), null, null, 'homeCollection', 'test01@localhost.de', 'de359448-1ee0-4151-872d-eea0ee462bc6', null, null, null, null, null, 2, null);

INSERT INTO item (collectionid, itemtype, id, etag, modifydate, clientcreatedate, clientmodifieddate, displayname, itemname, uid, icaluid, contentEncoding, contentLanguage, contentType, hasmodifications, ownerid, modifiesitemid)
VALUES (1, 'calendarcollection', 2, 'NVy57RJot0LhdYELkMDJ9gQZjOM=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), null, null, 'calendarDisplayName', 'calendar', 'a172ed34-0106-4616-bb40-a416a8305465', null, null, null, null, null, 2, null);

INSERT INTO item (collectionid, itemtype, id, etag, modifydate, clientcreatedate, clientmodifieddate, displayname, itemname, uid, icaluid, contentEncoding, contentLanguage, contentType, hasmodifications, ownerid, modifiesitemid)
VALUES (1, 'cardcollection', 3, 'njy57RJot0LhdYELkMDJ9gQZiOM=', to_timestamp('2015-11-21 21:11:00','YYYY-MM-DD HH:MI:SS'), null, null, 'contactDisplayName', 'contacts', 'a112ed14-0106-4616-bb40-a416a8305465', null, null, null, null, null, 2, null);