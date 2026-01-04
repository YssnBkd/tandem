# Deployment Notes: Partner System

**Feature**: 006-partner-system
**Scope**: Infrastructure and deployment requirements outside Kotlin codebase

## Web Fallback for Invite Links (FR-015)

**Requirement**: When the Tandem app is not installed, invite links (`https://tandem.app/invite/[code]`) must fall back to a web landing page.

**Implementation**: This is handled automatically by Android App Links / iOS Universal Links:
- If app is installed → deep link opens app directly
- If app is NOT installed → HTTPS URL loads in browser

**Action Required**:
1. Deploy a web landing page at `https://tandem.app/invite/[code]`
2. Page should:
   - Display inviter's name (fetch via Supabase public API)
   - Show app store download buttons (Google Play, App Store)
   - Explain the partner connection flow
3. This is a separate web project (not part of Kotlin codebase)

**Status**: Out of scope for `/speckit.implement` - requires separate web deployment

---

## Digital Asset Links (Android App Links Verification)

**Requirement**: For App Links to open the app without user prompt, the domain must verify app ownership.

**Action Required**:
1. Host `assetlinks.json` at: `https://tandem.app/.well-known/assetlinks.json`
2. Content:
```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "org.epoque.tandem",
    "sha256_cert_fingerprints": ["<RELEASE_SIGNING_KEY_FINGERPRINT>"]
  }
}]
```
3. Replace `<RELEASE_SIGNING_KEY_FINGERPRINT>` with actual release key SHA-256

**How to get fingerprint**:
```bash
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```

**Status**: Required before production release

---

## Supabase Edge Function: send-notification

**Requirement**: Push notifications via FCM require server-side sending.

**Action Required**:
1. Deploy Edge Function via Supabase CLI or dashboard
2. Configure FCM service account credentials as Edge Function secrets
3. Set up database webhook trigger on `notifications` table INSERT

**Status**: Covered by T-011 in tasks.md
