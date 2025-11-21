const state={ws:null,pc:null,callId:null,kind:"VOICE"}
const els={
  userId:document.getElementById("userId"),
  peerId:document.getElementById("peerId"),
  connectBtn:document.getElementById("connectBtn"),
  disconnectBtn:document.getElementById("disconnectBtn"),
  voiceCallBtn:document.getElementById("voiceCallBtn"),
  videoCallBtn:document.getElementById("videoCallBtn"),
  answerBtn:document.getElementById("answerBtn"),
  rejectBtn:document.getElementById("rejectBtn"),
  hangupBtn:document.getElementById("hangupBtn"),
  localVideo:document.getElementById("localVideo"),
  remoteVideo:document.getElementById("remoteVideo")
}
function wsUrl(userId){const origin=location.origin.replace("http","ws");const base=`${origin}`;return `${base}/demo/ws/call/${userId}`}
function connect(){const uid=els.userId.value.trim();if(!uid)return;state.ws=new WebSocket(wsUrl(uid));state.ws.onopen=()=>{};state.ws.onmessage=ev=>onSignal(JSON.parse(ev.data));state.ws.onclose=()=>{};}
function disconnect(){if(state.ws){state.ws.close();state.ws=null}}
async function setup(kind){state.kind=kind;state.pc=new RTCPeerConnection();state.pc.onicecandidate=e=>{if(e.candidate)send({action:"ICE",callId:state.callId,payload:{candidate:e.candidate.candidate},toUserId:parseInt(els.peerId.value)})}
const constraints=kind==="VIDEO"?{video:true,audio:true}:{audio:true};const stream=await navigator.mediaDevices.getUserMedia(constraints);stream.getTracks().forEach(t=>state.pc.addTrack(t,stream));els.localVideo.srcObject=stream;state.pc.ontrack=e=>{els.remoteVideo.srcObject=e.streams[0]}}
async function call(kind){await setup(kind);const offer=await state.pc.createOffer();await state.pc.setLocalDescription(offer);send({action:"INITIATE",toUserId:parseInt(els.peerId.value),callType:kind})
send({action:"SDP_OFFER",callId:state.callId,payload:{sdp:offer.sdp},toUserId:parseInt(els.peerId.value)})}
async function answer(){await setup(state.kind);send({action:"ANSWER",callId:state.callId,toUserId:parseInt(els.peerId.value)})}
function reject(){send({action:"REJECT",callId:state.callId,toUserId:parseInt(els.peerId.value)})}
function hangup(){send({action:"END",callId:state.callId,toUserId:parseInt(els.peerId.value)});cleanup()}
function cleanup(){if(state.pc){state.pc.getSenders().forEach(s=>{try{s.track&&s.track.stop()}catch{}});state.pc.close();state.pc=null}if(els.localVideo.srcObject){els.localVideo.srcObject.getTracks().forEach(t=>t.stop());els.localVideo.srcObject=null}els.remoteVideo.srcObject=null}
function send(obj){if(!state.ws||state.ws.readyState!==1)return;obj.timestamp=Date.now();state.ws.send(JSON.stringify(obj))}
async function onSignal(msg){if(msg.action==="INITIATE_ACK"){state.callId=msg.callId;return}
if(msg.action==="RINGING"){state.callId=msg.callId;state.kind=msg.payload&&msg.payload.callType?msg.payload.callType:"VOICE";return}
if(msg.action==="SDP_ANSWER"){await state.pc.setRemoteDescription({type:"answer",sdp:msg.payload.sdp});return}
if(msg.action==="SDP_OFFER"){state.callId=msg.callId;await setup(state.kind||"VOICE");await state.pc.setRemoteDescription({type:"offer",sdp:msg.payload.sdp});const ans=await state.pc.createAnswer();await state.pc.setLocalDescription(ans);send({action:"SDP_ANSWER",callId:state.callId,payload:{sdp:ans.sdp},toUserId:parseInt(els.peerId.value)});return}
if(msg.action==="ICE"){try{await state.pc.addIceCandidate({candidate:msg.payload.candidate})}catch{}return}
if(msg.action==="END"||msg.action==="REJECT"||msg.action==="CANCEL"){cleanup();return}}
els.connectBtn.onclick=connect;els.disconnectBtn.onclick=disconnect;els.voiceCallBtn.onclick=()=>call("VOICE");els.videoCallBtn.onclick=()=>call("VIDEO");els.answerBtn.onclick=answer;els.rejectBtn.onclick=reject;els.hangupBtn.onclick=hangup