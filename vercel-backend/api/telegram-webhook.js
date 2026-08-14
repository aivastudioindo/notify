const admin = require('firebase-admin');

// Inisialisasi Firebase Admin
if (!admin.apps.length) {
  try {
    const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
  } catch (error) {
    console.error('Firebase Admin init error:', error);
  }
}

module.exports = async (req, res) => {
  if (req.method !== 'POST') {
    return res.status(200).send('Famly Telegram Webhook is Active!');
  }

  const { message } = req.body || {};
  if (!message || !message.text) {
    return res.status(200).json({ status: 'ignored' });
  }

  const chatId = message.chat.id.toString();
  const text = message.text.toLowerCase().trim();
  const tokenFCM = process.env.CHILD_DEVICE_FCM_TOKEN;

  const botToken = process.env.TELEGRAM_BOT_TOKEN;

  async function sendTelegramMessage(msgText) {
    if (!botToken) return;
    try {
      await fetch(`https://api.telegram.org/bot${botToken}/sendMessage`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          chat_id: chatId,
          text: msgText,
          parse_mode: 'HTML'
        })
      });
    } catch (e) {
      console.error('Error sending Telegram msg:', e);
    }
  }

  if (text.startsWith('/lokasi') || text.startsWith('/location') || text.startsWith('/where')) {
    await sendTelegramMessage('⏳ <i>[Vercel FCM] Mengirimkan sinyal push ke HP anak... Mohon tunggu koordinat GPS.</i>');
    
    if (tokenFCM) {
      try {
        await admin.messaging().send({
          token: tokenFCM,
          data: {
            action: 'get_location',
            chat_id: chatId
          },
          android: {
            priority: 'high'
          }
        });
      } catch (err) {
        console.error('Error FCM:', err);
        await sendTelegramMessage(`⚠️ <b>Gagal Kirim FCM:</b> ${err.message}`);
      }
    } else {
      await sendTelegramMessage('⚠️ <b>Token FCM HP Anak belum dikonfigurasi di Environment Variable Vercel (CHILD_DEVICE_FCM_TOKEN).</b>');
    }
  } else if (text === '/ping') {
    if (tokenFCM) {
      try {
        await admin.messaging().send({
          token: tokenFCM,
          data: {
            action: 'ping',
            chat_id: chatId
          },
          android: {
            priority: 'high'
          }
        });
        await sendTelegramMessage('📡 <i>Sinyal Ping dikirim ke HP anak via FCM...</i>');
      } catch (err) {
        await sendTelegramMessage(`⚠️ <b>Gagal Ping FCM:</b> ${err.message}`);
      }
    } else {
      await sendTelegramMessage('✅ <b>Famly Webhook Vercel Online!</b> (Token FCM anak belum terpasang)');
    }
  } else if (text === '/start' || text === '/help') {
    await sendTelegramMessage('👋 <b>Selamat Datang di Bot Famly (Vercel Serverless)!</b>\n\nPerintah:\n• <code>/lokasi</code> - Panggil GPS HP anak via FCM\n• <code>/ping</code> - Cek koneksi HP anak');
  }

  return res.status(200).json({ status: 'success' });
};
