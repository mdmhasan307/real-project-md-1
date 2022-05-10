if (!HTMLFormElement.prototype.reportValidity) {
    HTMLFormElement.prototype.reportValidity = function () {
        if (this.checkValidity())
            return true;
        var btn = document.createElement('input');
        btn.setAttribute("type", "submit");
        this.appendChild(btn);
        btn.click();
        this.removeChild(btn);
        return false;
    };
}
