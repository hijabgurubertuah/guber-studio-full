const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Any user/device can read app_config settings", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("app_config").doc("settings").set({
      targetUrl: "https://share.gemini.google/MgVLfdtGxua0",
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("app_config").doc("settings").get());
});

test("Valid URL update to app_config settings succeeds", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(
    db.collection("app_config").doc("settings").set({
      targetUrl: "https://new-url.example.com",
      updatedBy: "admin",
    })
  );
});

test("Invalid URL or missing targetUrl is rejected", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  // Missing targetUrl
  await assertFails(
    db.collection("app_config").doc("settings").set({
      updatedBy: "admin",
    })
  );

  // Invalid protocol (not http or https)
  await assertFails(
    db.collection("app_config").doc("settings").set({
      targetUrl: "javascript:alert(1)",
    })
  );
});

test("Deleting config is forbidden", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("app_config").doc("settings").set({
      targetUrl: "https://share.gemini.google/MgVLfdtGxua0",
    });
  });

  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(db.collection("app_config").doc("settings").delete());
});

test("Unknown collection access is denied", async () => {
  const db = testEnv.unauthenticatedContext().firestore();
  await assertFails(db.collection("secrets").doc("item").get());
});
