const express = require('express')
const randomstring = require("randomstring");

const app = express();
const port = 3000;

hashes = {
  keh783r: ["016068793", "Rahul Pillai"],
  eng274t: ["016068794", "Faizan Shaikh"],
  ien582g: ["016068795", "Adil Ansari"],
};

currHashes = ["keh783r"];

creds = {};

app.use(express.json());

app.post('/auth', (req, res) => {
  if (!req?.body?.hash || !req?.body?.id) {
    console.log("Bad generate request: Hash or ID is absent!");
    res.status(500);
    return;
  }

  if (!hashes[req?.body?.hash] || hashes[req?.body?.hash][0] != req?.body?.id) {
    console.log("Bad generate request: User is unauthenticated!");
    res.status(401);
    return;
  }

  console.log("User authenticated.");
  res.send(hashes[req?.body?.hash]);
});

app.get('/user', (req, res) => {
  if (!req?.query?.hash) {
    console.log("Bad access request: Hash is absent!");
    res.status(500);
    return;
  }

  if (!hashes[String(req?.query?.hash)]) {
    console.log("Bad access request: Hash is not in list of hashes!");
    res.status(401);
    return;
  }

  console.log("Profile page served");
  res.send(hashes[req?.query?.hash]);
});

app.post('/addCode', (req, res) => {
  if (!req?.headers?.hash) {
    console.log("Bad generate request: Hash is absent!");
    res.status(500).send("0");
    return;
  }

  if (!req?.body?.code) {
    console.log("Bad generate request: Code is absent!");
    res.status(500).send("0");
    return;
  }

  if (!hashes[req?.headers?.hash]) {
    console.log("Bad generate request: User is unauthenticated!");
    res.status(401).send("0");
    return;
  }

  // const code = randomstring.generate(7);
  // const pass = randomstring.generate(7);
  // const key = code + ':' + pass;
  creds[req?.headers?.hash] = req?.body?.code;
  console.log(`Added ${req?.body?.code} for ${req?.headers?.hash}.`);
  res.send("1");
});

app.get('/getCodes', (req, res) => {
  res.send(Object.values(creds).join(','));
});

app.post('/popCode', (req, res) => {
  if (!req?.body?.hash) {
    console.log("Bad unlock request: No credentials supplied. Could be an imposter!");
    res.status(401).send("0");
    return;
  }
  if (!creds[req?.body?.hash] || !currHashes.includes(req?.body?.hash)) {
    console.log("Bad unlock request: Bad credentials supplied. Could be an imposter!");
    res.status(401).send("0");
    return;
  }
  delete creds[req?.body?.hash];

  console.log("Gate open request served.");
  res.send("1");
});

app.listen(port, () => {
  console.log(`Tap-n-Auth app listening on port ${port}`);
});