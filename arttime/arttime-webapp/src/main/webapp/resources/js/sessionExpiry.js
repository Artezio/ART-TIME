function sessionExpirationSocketCloseListener(code, channel, event) {
    if (code === 1000 || code === 1008) {
        if (!window.loggingOut) {
            PF('sessionExpiredDialog').show();
        }
        return true;
    }
}