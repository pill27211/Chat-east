const admin = require('firebase-admin');
const firebase_service_account = require('./service_account_key.json');

admin.initializeApp({
  credential: admin.credential.cert(firebase_service_account)
});

async function testFirebaseConnection() {
  try {
    // Send a test message to verify the connection
    const response = await admin.messaging().send({
      notification: {
        title: 'Firebase Admin SDK Initialization Test',
        body: 'This is a test message to verify Firebase Admin SDK initialization.'
      },
      topic: 'test' // Use a test topic; this should be replaced with a valid topic or device token in a real-world scenario
    });
    console.log('Firebase Admin SDK initialized successfully. Test message sent:', response);
  } catch (error) {
    console.error('Error initializing Firebase Admin SDK or sending test message:', error);
  }
}

testFirebaseConnection();

module.exports = admin;
