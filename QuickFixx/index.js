const functions = require('firebase-functions');

const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);


exports.UpdateIntroduction = functions.https.onRequest((req,res)=>{
  var db = admin.database();
  var ref = db.ref();

  var usersRef = ref.child("users");
  var data = req.query.text;
  var JSONobject = JSON.parse(data);
  var UserEmail = JSONobject.UserEmail;
  var Introduction = JSONobject.Introduction;

  usersRef.child(UserEmail).update({Introduction: Introduction}).then(snapshot=>{
    res.send('IntroductionUpdated');
  });
});

exports.UpdateEducation = functions.https.onRequest((req,res)=>{
  var db = admin.database();
  var ref = db.ref();

  var usersRef = ref.child("users");
  var data = req.query.text;
  var JSONobject = JSON.parse(data);
  var UserEmail = JSONobject.UserEmail;
  var School = JSONobject.School;
  var College = JSONobject.College;
  var University = JSONobject.University;

  usersRef.child(UserEmail).update({School: School,College: College,University: University}).then(snapshot=>{
    res.send('EducationUpdated');
  });
});

exports.AcceptBid = functions.https.onRequest((req,res) =>{
  var db = admin.database();
  var ref = db.ref();

  var bidsRef = ref.child("bids");
  var jobsRef = ref.child("jobs");
  var data = req.query.text;
  var JSONobject = JSON.parse(data);
  var bidId = JSONobject.BidId;
  var jobId = JSONobject.JobId;
  var bidBy = JSONobject.BidBy;

  var status = "accepted";

  bidsRef.child(jobId).child(bidId).update({status: status}).then(snapshot=>{
    jobsRef.child(jobId).update({assignedTo: bidBy}).then(snapshot=>{
      res.send('BidAccepted');
    });
  });
});

exports.RejectBid = functions.https.onRequest((req,res) =>{
  var db = admin.database();
  var ref = db.ref();

  var usersRef = ref.child("bids");
  var data = req.query.text;
  var JSONobject = JSON.parse(data);
  var bidId = JSONobject.BidId;
  var jobId = JSONobject.JobId;

  var status = "rejected";

  usersRef.child(jobId).child(bidId).update({status: status}).then(snapshot=>{
    res.send('BidRejected');
  });
});

exports.UpdateResidence = functions.https.onRequest((req,res)=>{
  var db = admin.database();
  var ref = db.ref();

  var usersRef = ref.child("users");
  var data = req.query.text;
  var JSONobject = JSON.parse(data);
  var UserEmail = JSONobject.UserEmail;
  var Address = JSONobject.Address;

  usersRef.child(UserEmail).update({Address: Address}).then(snapshot=>{
    res.send('ResidenceUpdated');
  });
});

exports.UpdateContact = functions.https.onRequest((req,res)=>{
  var db = admin.database();
  var ref = db.ref();

  var usersRef = ref.child("users");
  var data = req.query.text;
  var JSONobject = JSON.parse(data);
  var UserEmail = JSONobject.UserEmail;
  var Phone = JSONobject.Phone;

  usersRef.child(UserEmail).update({Phone: Phone}).then(snapshot=>{
    res.send('ContactUpdated');
  });
});

exports.InsertBasicUserInfo = functions.https.onRequest((req,res) => {
  var db = admin.database();
  var ref = db.ref();

  var usersRef = ref.child("users");
  var data = req.query.text;
  var JSONobject = JSON.parse(data);
  var fname = JSONobject.FirstName;
  var lname = JSONobject.LastName;
  var email = JSONobject.Email;
  var phone = JSONobject.Phone;
  var address = JSONobject.Address;
  var verificationstatus = JSONobject.VerificationStatus;
  var profilepicurl = JSONobject.ProfilePicUrl;
  var type = JSONobject.Type;
  var key = JSONobject.key;
  var introduction = JSONobject.Introduction;
  var age = JSONobject.Age;
  var school = JSONobject.School;
  var college = JSONobject.College;
  var university = JSONobject.University;

  var wholeData = {
    FirstName: fname,
    LastName: lname,
    Email: email,
    Phone: phone,
    Address: address,
    VerificationStatus: verificationstatus,
    ProfilePicUrl: profilepicurl,
    Type: type,
    Introduction: introduction,
    Age: age,
    School: school,
    College: college,
    University: university
};
 usersRef.child(key).set(wholeData).then(snapshot =>{
    res.send('ok');
 });
 });

 exports.VoteUp = functions.https.onRequest((req,res) => {
   var db = admin.database();
   var ref = db.ref();

   var usersRef = ref.child("jobs");
   var usersRef2 = ref.child("voting");


   var data = req.query.text;
   var JSONobject = JSON.parse(data);
   var JobId = JSONobject.JobId;
   var voteUp = JSONobject.voteUp;
   var UserEmail = JSONobject.UserEmail;

   var newref = usersRef2.child(JobId).push();
   var key = newref.key;
   var wholeData = {
     UserEmail: UserEmail,
     Vote: "1",
     key: key
   };

   var newVotes = parseInt(voteUp)+1;
   var newVotess = newVotes.toString();
   usersRef.child(JobId).update({voteUp: newVotess}).then(snapshot => {
     usersRef2.child(JobId).child(key).set(wholeData).then(snapshot=>{
       res.send('ok');
     });
   });
 });

 exports.VoteDown = functions.https.onRequest((req,res)=>{
   var db = admin.database();
   var ref = db.ref();

   var usersRef = ref.child("jobs");
   var usersRef2 = ref.child("voting");

   var data = req.query.text;
   var JSONobject = JSON.parse(data);
   var JobId = JSONobject.JobId;
   var voteDown = JSONobject.voteDown;
   var UserEmail = JSONobject.UserEmail;

   var newref = usersRef2.child(JobId).push();
   var key = newref.key;
   var wholeDataa = {
     UserEmail: UserEmail,
     Vote: "0",
     key: key
   };

   var newVotes = parseInt(voteDown)+1;
   var newVotess = newVotes.toString();
   usersRef.child(JobId).update({voteDown: newVotess}).then(snapshot => {
     usersRef2.child(JobId).child(key).set(wholeDataa).then(snapshot=>{
       res.send('ok');
     });
   });
 });

 exports.InsertJobInfo = functions.https.onRequest((req,res) => {
   var db = admin.database();
   var ref = db.ref();

   var usersRef = ref.child("jobs");
   var data = req.query.text;
   var JSONobject = JSON.parse(data);
   var jobTitle = JSONobject.title;
   var jobDescription = JSONobject.description;
   var postedBy = JSONobject.postedBy;
   var coins = JSONobject.coins;
   var status = JSONobject.status;
   var visible = JSONobject.visible;
   var timeStamp = JSONobject.timestamp;
   var latitude = JSONobject.latitude;
   var longitude = JSONobject.longitude;
   var voteUp = JSONobject.voteUp;
   var voteDown = JSONobject.voteDown;
   var totalBids = JSONobject.totalBids;
   var assignedTo = JSONobject.assignedTo;
   var sponsoredBy = JSONobject.sponsoredBy;
   var maxNoDays = JSONobject.maxNoDays;
   var address = JSONobject.address;
   var key = JSONobject.key;

   var wholeData = {
     jobTitle: jobTitle,
     jobDescription: jobDescription,
     postedBy: postedBy,
     coins: coins,
     status: status,
     visible: visible,
     timeStamp: timeStamp,
     latitude: latitude,
     longitude: longitude,
     voteUp: voteUp,
     voteDown: voteDown,
     totalBids: totalBids,
     assignedTo: assignedTo,
     sponsoredBy: sponsoredBy,
     maxNoDays: maxNoDays,
     address: address,
     JobId: key
   };
   usersRef.child(key).set(wholeData).then(snapshot => {
     res.send('ok');
   });
 });

 exports.BookmarkPost = functions.https.onRequest((req,res)=>{
   var db = admin.database();
   var ref = db.ref();

   var bookmarksRef = ref.child("bookmarks");
   var data = req.query.text;

   var JSONobject = JSON.parse(data);
   var JobId = JSONobject.JobId;
   var bookmarkBy = JSONobject.bookmarkBy;

   var newref = bookmarksRef.child(JobId).push();
   var key = newref.key;

   var wholeData = {
     JobId: JobId,
     key: key
   };
   bookmarksRef.child(bookmarkBy).child(key).set(wholeData).then(snapshot=>{
     res.send('bookmark Successful');
   });

 });

exports.InsertCoins = functions.https.onRequest((req,res)=>{
  var db = admin.database();
  var ref = db.ref();

  var coinsRef = ref.child("coins");
  var data = req.query.text;

  var JSONobject = JSON.parse(data);
  var userEmail = JSONobject.user;
  var coins = JSONobject.coins;

  var wholeData = {
    coins: coins
  };

  coinsRef.child("balance").child(userEmail).set(wholeData).then(snapshot=>{
    res.send('ok');
  });
 });

 exports.SponsorJob = functions.https.onRequest((req,res)=>{
   var db = admin.database();
   var ref = db.ref();

   var jobsRef = ref.child("jobs");
   var data = req.query.text;

   var JSONobject = JSON.parse(data);
   var JobId = JSONobject.JobId;
   var sponsoredBy = JSONobject.sponsoredBy;

   jobsRef.child(JobId).update({sponsoredBy: sponsoredBy}).then(snapshot=>{
     res.send('SponsorSuccessful');
   });

 });

 exports.InsertProgress = functions.https.onRequest((req,res)=>{
   var db = admin.database();
   var ref = db.ref();

   var progressRef = ref.child("progress");
   var data = req.query.text;

   var JSONobject = JSON.parse(data);
   var JobId = JSONobject.JobId;
   var key = JSONobject.key;
   var ProgressDescription = JSONobject.ProgressDescription;

   var wholeData = {
     JobId: JobId,
     key: key,
     ProgressDescription: ProgressDescription
   };
   progressRef.child(JobId).child(key).set(wholeData).then(snapshot=>{
     res.send('ProgressAdded');
   });
 });

 exports.InsertProgressGallery = functions.https.onRequest((req,res)=>{
   var db = admin.database();
   var ref = db.ref();

   var progressGalleryRef = ref.child("progressGallery");
   var data = req.query.text;

   var JSONobject = JSON.parse(data);
   var JobId = JSONobject.JobId;
   var key = JSONobject.key;
   var ImageUrl = JSONobject.ImageUrl;

   var wholeData = {
     JobId: JobId,
     key: key,
     ImageUrl: ImageUrl
   };
   progressGalleryRef.child(JobId).child(key).set(wholeData).then(snapshot=>{
     res.send('ProgressGalleryAdded');
   });
 });

 exports.InsertNewBid = functions.https.onRequest((req,res)=>{
   var db = admin.database();
   var ref = db.ref();

   var refff = ref.child("jobs");
   var reff = ref.child("bids");

   var data = req.query.text;
   var JSONobject = JSON.parse(data);
   var JobId = JSONobject.JobId;
   var BidBy = JSONobject.BidBy;
   var BidCoins = JSONobject.BidCoins;
   var description = JSONobject.description;
   var timeStamp = JSONobject.timeStamp;
   var status = JSONobject.status;
   var totalBids = JSONobject.totalBids;

   var newBids = parseInt(totalBids)+1;
   var newBidss = newBids.toString();

   var newref = reff.child(JobId).push();
   var key = newref.key;

   var wholeData = {
     JobId: JobId,
     bidBy: BidBy,
     bidCoins: BidCoins,
     description: description,
     timeStamp: timeStamp,
     status: status,
     key: key
   };

   refff.child(JobId).update({totalBids: newBidss}).then(snapshot => {
     reff.child(JobId).child(key).set(wholeData).then(snapshot =>{
       res.send('ok');
     });
   });
 });

 exports.InsertDataToImageGallery = functions.https.onRequest((req,res) => {
   var db = admin.database();
   var ref = db.ref();

   var usersRef = ref.child("imageGallery");
   var data = req.query.text;
   var JSONobject = JSON.parse(data);
   var ImageUrl = JSONobject.ImageUrl;
   var description = JSONobject.Description;
   var cover = JSONobject.Cover;
   var timeStamp = JSONobject.timestamp;
   var key = JSONobject.key;

   var wholeData = {
     ImageUrl: ImageUrl,
     description: description,
     cover: cover,
     timeStamp: timeStamp,
     JobId: key
   };
   usersRef.child(key).set(wholeData).then(snapshot => {
     res.send('ok');
   });
 });

 exports.CheckUserExistsOrNot = functions.https.onRequest((req,res)=>{
   var db = admin.database();
   var ref = db.ref();

   var usersRef = ref.child("users");
   var email = req.query.text;

   usersRef.orderByChild("Email").equalTo(email).once("value",snapshot=>{
     const exists = snapshot.val();
     if(exists){
       res.send('exists');
     }
     else {
       res.send('notexists');
     }
   });
 });

 exports.retreiveJobsFromDatabase = functions.https.onRequest((req,res) => {
   var db = admin.database();
   var ref = db.ref();

   function snapshotToArray(snapshot) {
       var returnArr = [];

       snapshot.forEach(function(childSnapshot) {
           var item = childSnapshot.val();
           // item.key = childSnapshot.key;

           returnArr.push(item);
       });

       return returnArr;
   };
   var usersRef = ref.child("jobs");
   usersRef.on("value", function(snapshot){
     res.send(snapshotToArray(snapshot));
   });
 });

 exports.RejectAllOtherBids = functions.https.onRequest((req,res)=>{
   var db = admin.database();
   var ref = db.ref();

   var usersRef = ref.child("bids");
   var data = req.query.text;
   var JSONobject = JSON.parse(data);
   var bidId = JSONobject.BidId;
   var jobId = JSONobject.JobId;

   var reff = usersRef.child(jobId);
   var status = "rejected";
   reff.on("value",function(snapshot){
     snapshot.forEach(function(childSnapshot){
       if (childSnapshot.key != bidId) {
         reff.child(childSnapshot.key).update({status: status});
       }
     });
     res.send('RejectAllOtherBids');
   });
 });

 exports.retreiveUsersFromDatabase = functions.https.onRequest((req,res) => {
   var db = admin.database();
   var ref = db.ref();

   function snapshotToArray(snapshot) {
       var returnArr = [];

       snapshot.forEach(function(childSnapshot) {
           var item = childSnapshot.val();
           // item.key = childSnapshot.key;

           returnArr.push(item);
       });

       return returnArr;
   };


   var usersRef = ref.child("users");
   usersRef.on("value", function(snapshot){
     res.send(snapshotToArray(snapshot));
   });
 });
