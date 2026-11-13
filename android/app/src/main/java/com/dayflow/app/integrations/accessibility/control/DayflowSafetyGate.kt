package com.dayflow.app.integrations.accessibility.control

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Safety gate for the screen-control layer (AURA's RestrictedAppAccessibilityGate
 * pattern, re-implemented for Dayflow's scope).
 *
 * Two hard rules, independent of any user setting:
 * 1. **Restricted apps** — banking, payment and authentication packages are
 *    never driven. Many of those apps refuse to run (or refuse login) while an
 *    accessibility service is enabled at all, so Dayflow also stays passive
 *    there: no perception of their content is forwarded, no gesture performed.
 * 2. **No credentials** — text is never typed into password / PIN / OTP fields
 *    and never into secure nodes ([AccessibilityNodeInfo.isPassword]).
 */
class DayflowSafetyGate {

    data class Decision(val allowed: Boolean, val reason: String)

    fun checkPackage(packageName: String): Decision {
        if (packageName.isBlank()) return Decision(false, "unknown package")
        val lowered = packageName.lowercase()
        if (restrictedPackages.any { lowered == it || lowered.contains(it) }) {
            return Decision(false, "restricted app: $packageName")
        }
        return Decision(true, "allowed")
    }

    fun checkTypeTarget(node: AccessibilityNodeInfo, text: String): Decision {
        if (node.isPassword) {
            return Decision(false, "refusing to type into a password field")
        }
        val hint = node.hintText?.toString()?.lowercase().orEmpty()
        val label = node.text?.toString()?.lowercase().orEmpty()
        val desc = node.contentDescription?.toString()?.lowercase().orEmpty()
        val context = "$hint $label $desc"
        if (credentialHints.any { context.contains(it) }) {
            return Decision(false, "refusing to type into a credential field")
        }
        // Never type OTP-shaped content ("123456") into anything labeled verify/code.
        if (text.length <= 8 && text.all { it.isDigit() } &&
            context.containsAny(listOf("verify", "code", "otp", "pin"))
        ) {
            return Decision(false, "refusing to type digit-code into a verification field")
        }
        return Decision(true, "allowed")
    }

    private fun String.containsAny(needles: List<String>): Boolean {
        return needles.any { contains(it) }
    }

    companion object {
        /**
         * Package fragments treated as finance/banking/identity — never driven.
         * Matched case-insensitively against the foreground package name.
         */
        val restrictedPackages = listOf(
            "bank", "banking", "paypal", "klarna", "revolut", "n26",
            "sparkasse", "volksbank", "commerzbank", "deutschebank",
            "ing.", "dkb", "comdirect", "trade212", "binance", "coinbase",
            "wallet", "venmo", "cashapp", "wise.", "santander", "hsbc",
            "chase", "wellsfargo", "citi", "amex", "fidelity", "vanguard",
            "1password", "bitwarden", "keepass", "lastpass", "authenticator",
            "google.android.gms" // covers Google login / Play services auth flows
        )

        /** Text fragments that mark a field as a credential sink. */
        val credentialHints = listOf(
            "password", "passwort", "contraseña", "pin ", "pin:", "otp",
            "one-time", "einmalcode", "verification code", "bestätigungscode"
        )
    }
}
