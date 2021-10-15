var grade = -1;
var resourcePath = valuateServiceUrl + "/faces/javax.faces.resource";

function valuateClose() {
    var valuate = document.getElementById("valuate");
    valuate.parentElement.removeChild(valuate);
}

function valuateLoad() {
    var valuate = document.getElementById("valuate");

    // Setting default style
    var style = document.createElement("link");
    style.setAttribute("rel", "stylesheet");
    style.setAttribute("href", resourcePath + "/snippet.css?ln=css");
    valuate.appendChild(style);

    // Logo area
    var logoArea = document.createElement("div");
    logoArea.setAttribute("id", "valuate_logo_area");
    
    // Valuate branding
    var brandingImg = document.createElement("img");
    brandingImg.setAttribute("id", "valuate_logo");
    brandingImg.setAttribute("src", resourcePath + "/valuate_logo.png?ln=images");

    logoArea.appendChild(brandingImg);

    // User branding
    var userLogoPath = valuate.getAttribute("user-logo");
    if (userLogoPath !== null) {
        var userImage = document.createElement("img");
        userImage.setAttribute("id", "user_logo");
        userImage.setAttribute("src", userLogoPath);
        logoArea.appendChild(userImage);
    }

    valuate.appendChild(logoArea);

    // Close button
    var closeLink = document.createElement("button");
    closeLink.setAttribute("id", "valuate_close");
    closeLink.setAttribute("onClick", "valuateClose()");
    closeLink.innerHTML = "×";
    valuate.appendChild(closeLink);

    // Rearanged before valuate_question
    valuate.insertBefore(logoArea, document.getElementById("valuate_question"));
    valuate.insertBefore(closeLink, logoArea);

    // Lowest emoji caption
    var lowestValue = valuate.getAttribute("lowest");
    if (lowestValue !== null) {
        var lowest = document.createElement("div");
        lowest.id = "valuate_lowest";
        lowest.innerHTML = lowestValue;
        valuate.appendChild(lowest);
    }

    var emoticons = document.createElement("div");
    emoticons.setAttribute("id", "valuate_emoticons");

    var emojiType = document.getElementById("valuate").getAttribute("emoji");
    if (emojiType === null) {
        emojiType = "bw";   // Mare's default
    }

    for (let i = 1; i <= 5; i++) {
        var img = document.createElement("img");
        img.setAttribute("id", "valuate_grade_" + i);
        img.setAttribute("onClick", "valuateGrade(" + i + ")");

        img.setAttribute("src", resourcePath + "/" + i + ".png?ln=emoji_" + emojiType);

        emoticons.appendChild(img);
    }

    valuate.appendChild(emoticons);
    
    var highestValue = valuate.getAttribute("highest");
    if (highestValue !== null) {
        var highest = document.createElement("div");
        highest.id = "valuate_highest";
        highest.innerHTML = highestValue;
        valuate.appendChild(highest);
    }

    var commentArea = document.createElement("textarea");
    commentArea.setAttribute("id", "valuate_comment");
    commentArea.setAttribute("onChange", "valuateEnableSend()");
    commentArea.setAttribute("onKeyUp", "valuateEnableSend()");

    valuate.appendChild(commentArea);

    var submitButton = document.createElement("button");
    submitButton.setAttribute("id", "valuate_submit");
    submitButton.setAttribute("onClick", "valuateSend()");
    submitButton.appendChild(document.createTextNode("Send Evaluation"));
    submitButton.disabled = true;

    valuate.appendChild(submitButton);
}

function resetGrade() {
    if (grade > 0) {
        var selected = document.getElementById("valuate_grade_" + grade);
        selected.removeAttribute("class");
    }
    grade = -1;
}

function valuateEnableSend() {
    var submitButton = document.getElementById("valuate_submit");
    //submitButton.disabled = (document.getElementById("valuate_comment").value === "" || grade < 1);
    submitButton.disabled = (grade < 1);
}

function valuateGrade(x) {
    resetGrade();
    grade = x;
    document.getElementById("valuate_grade_" + x).setAttribute("class", "valuate_selected_grade");
    valuateEnableSend();
}

function valuateSend() {
    var xhttp = new XMLHttpRequest();
    xhttp.open("POST", valuateServiceUrl + "/endpoint", false);

    var data = {};

    data.comment = document.getElementById("valuate_comment").value;
    data.grade = grade;

    xhttp.send(JSON.stringify(data));

    var valuate = document.getElementById("valuate");
    //valuate.removeChild(document.getElementById("valuate_logo_area"));
    valuate.removeChild(document.getElementById("valuate_question"));
    valuate.removeChild(document.getElementById("valuate_emoticons"));
    valuate.removeChild(document.getElementById("valuate_comment"));
    valuate.removeChild(document.getElementById("valuate_submit"));
    
    var submitMessage = document.createElement("div");
    submitMessage.id = "valuate_submit_message";
    valuate.appendChild(submitMessage);
    
    if (xhttp.status === 200) {
        submitMessage.innerHTML = "Evaluation sent successfuly";
    } else {
        submitMessage.innerHTML = "Failed to send an evaluation";
    }
}
