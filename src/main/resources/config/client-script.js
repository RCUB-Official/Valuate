const valuateResourcePath = valuateServiceUrl + "/faces/javax.faces.resource";  // valuateServiceUrl is appended as a constant by the servlet.
const valuateElementName = "valuate";   // Just to make re-branding easier in case of the forking of the project
const servicePrefix = "valuate_";

const defaultTitle = "Valuate Feedback";

const valuateMinGrade = 1;  // Still must be integer.
const valuateMaxGrade = 5;  // Make sure there are emojis provided for each grade.

const useDefaultHighestLowest = true;
const defaultLowestValue = "Leftmost value";
const defaultHighestValue = "Rightmost value";

const identificationFormat = "You will be identified as {0}.";
const anonymousFormat = "Your feedback will be sent anonymously.";

const submitSuccessMessage = "Evaluation sent successfuly";
const submitFailMessage = "Failed to send an evaluation";

String.format = function () {   // Just to be able to make formatted strings for messages.
    var s = arguments[0];
    for (var i = 0; i < arguments.length - 1; i += 1) {
        var reg = new RegExp('\\{' + i + '\\}', 'gm');
        s = s.replace(reg, arguments[i + 1]);
    }
    return s;
};

function valuateLoad() {
    var valuates = document.getElementsByTagName(valuateElementName);
    for (var i = 0; i < valuates.length; i++) {
        if (valuates[i].getAttribute("id") === null) { // Generate an identifier
            valuates[i].id = servicePrefix + autoPrefix + i;
        } else {
            if (!valuates[i].id.startsWith(servicePrefix)) {    // Prefix an existing identifier, to avoid any potential collisions
                valuates[i].id = servicePrefix + valuates[i].id;
            }
        }

        valuateBuildElement(valuates[i]);
    }
}

function valuateBuildElement(valuate) {
    var valuateId = valuate.getAttribute("id");

    // Question text
    var question = document.createElement("td");    // Will be appended later in the rendering.
    question.id = valuateId + "_question";
    question.innerHTML = valuate.innerHTML;
    question.setAttribute("class", "valuate-question");
    valuate.innerHTML = ""; // Clearing the space for rendering.

    // Setting default style
    var style = document.createElement("link");
    style.setAttribute("rel", "stylesheet");
    style.setAttribute("href", valuateResourcePath + "/snippet.css?ln=css");
    valuate.appendChild(style);

    // Title Bar
    var headerTable = document.createElement("table");
    headerTable.setAttribute("cellspacing", "0");
    var titleBarRow = document.createElement("tr");

    headerTable.appendChild(titleBarRow);

    var titleColumn = document.createElement("th");
    titleColumn.setAttribute("class", "title-cell");
    titleColumn.setAttribute("colspan", "2");

    var titleValue = valuate.getAttribute("title");
    if (titleValue !== null) {
        titleColumn.innerHTML = titleValue;
    } else {
        titleColumn.innerHTML = defaultTitle;
    }

    titleBarRow.appendChild(titleColumn);

    var closeColumn = document.createElement("th");
    closeColumn.setAttribute("class", "close-cell");

    // Close button
    var closeLink = document.createElement("button");
    closeLink.setAttribute("class", "valuate-close");
    closeLink.setAttribute("onClick", "valuateClose(\"" + valuateId + "\")");
    closeLink.innerHTML = "×";
    closeColumn.appendChild(closeLink);

    titleBarRow.appendChild(closeColumn);


    // Logo area
    var logoArea = document.createElement("tr");

    var valuateLogoColumn = document.createElement("td");

    // Valuate branding
    var valuateLink = document.createElement("a");
    valuateLink.target = "_blank";
    valuateLink.href = valuateServiceUrl;
    var brandingImg = document.createElement("img");
    brandingImg.setAttribute("class", "valuate-logo");

    brandingImg.setAttribute("class", "valuate-logo");
    brandingImg.src = valuateResourcePath + "/valuate-logo.png?ln=images";
    valuateLink.appendChild(brandingImg);

    valuateLogoColumn.appendChild(valuateLink);
    logoArea.appendChild(valuateLogoColumn);

    // Question
    logoArea.appendChild(question);

    // User branding
    var userLogoColumn = document.createElement("td");

    var userLogoPath = valuate.getAttribute("user-logo");
    var userImage = null;
    if (userLogoPath !== null) {
        userImage = document.createElement("img");
        userImage.setAttribute("class", "user-logo");
        userImage.src = userLogoPath;
    }

    var userUrl = valuate.getAttribute("user-link");
    var userLink = null;
    if (userUrl !== null) {
        userLink = document.createElement("a");
        userLink.href = userUrl;
        userLink.target = "_blank";
    }

    if (userLink !== null) {
        if (userImage !== null) {
            userLink.appendChild(userImage);
            userLogoColumn.appendChild(userLink);
        }
    } else {
        if (userImage !== null) {
            userLogoColumn.appendChild(userImage);
        }
    }
    logoArea.appendChild(userLogoColumn);

    headerTable.appendChild(logoArea);

    valuate.appendChild(headerTable);

    var emojiTable = document.createElement("table");
    emojiTable.setAttribute("class", "valuate-emoticons");

    var emoticons = document.createElement("tr");
    emojiTable.appendChild(emoticons);

    var emojiType = valuate.getAttribute("emoji");
    if (emojiType === null) {
        emojiType = "bw";   // Mare's default
    }

    for (let i = valuateMinGrade; i <= valuateMaxGrade; i++) {
        var column = document.createElement("td");
        column.align = "center";
        column.setAttribute("class", "valuate-emoticon-cell");

        var img = document.createElement("img");
        img.id = valuateId + "_grade_" + i;
        img.setAttribute("onClick", "valuateGrade('" + valuateId + "', " + i + ")");

        img.src = valuateResourcePath + "/" + i + ".png?ln=emoji_" + emojiType;

        if (i === valuateMinGrade) {
            img.alt = valuateMinGrade + " - lowest";
        } else if (i === valuateMaxGrade) {
            img.alt = valuateMaxGrade + " - highest";
        } else {
            img.alt = i;
        }

        column.appendChild(img);
        emoticons.appendChild(column);

        if (i < valuateMaxGrade) {    // Spacing table
            var spacer = document.createElement("td");
            emoticons.appendChild(spacer);
        }
    }
    valuate.appendChild(emojiTable);

    var lowestValue = valuate.getAttribute("lowest");
    if (lowestValue === null && useDefaultHighestLowest) {
        lowestValue = defaultLowestValue;
    }

    var highestValue = valuate.getAttribute("highest");
    if (highestValue === null && useDefaultHighestLowest) {
        highestValue = defaultHighestValue;
    }

    if (lowestValue !== null || highestValue !== null) {
        var explanations = document.createElement("div");
        explanations.setAttribute("class", "valuate-explanations");

        if (lowestValue !== null) {
            var lowest = document.createElement("div");
            lowest.id = valuateId + "_lowest";
            lowest.setAttribute("class", "valuate-lowest");
            lowest.innerHTML = lowestValue;
            explanations.appendChild(lowest);
        }

        if (highestValue !== null) {
            var highest = document.createElement("div");
            highest.id = valuateId + "_highest";
            highest.setAttribute("class", "valuate-highest");
            highest.innerHTML = highestValue;
            explanations.appendChild(highest);
        }
        valuate.appendChild(explanations);
    }


    var commentArea = document.createElement("textarea");
    commentArea.id = valuateId + "_comment";
    commentArea.setAttribute("class", "valuate-comment");
    commentArea.setAttribute("onChange", "valuateEnableSend('" + valuateId + "')");
    commentArea.setAttribute("onKeyUp", "valuateEnableSend('" + valuateId + "')");

    valuate.appendChild(commentArea);

    // Valuator ID
    var valuatorId = valuate.getAttribute("valuator-id");
    if (valuatorId !== null) {
        var user = document.createElement("div");
        user.setAttribute("class", "valuate-user-id");

        var identifyCheckbox = document.createElement("input");
        identifyCheckbox.id = valuateId + "_identify";
        identifyCheckbox.setAttribute("onChange", "valuateChangeIdentify(\"" + valuateId + "\")");
        identifyCheckbox.setAttribute("type", "checkbox");
        identifyCheckbox.setAttribute("checked", "");
        identifyCheckbox.setAttribute("class", "valuate-checkbox");
        user.appendChild(identifyCheckbox);

        var identifyLabel = document.createElement("span");
        identifyLabel.innerHTML = String.format(identificationFormat, valuatorId);
        identifyLabel.id = valuateId + "_identify_label";

        user.appendChild(identifyLabel);
        valuate.appendChild(user);
    }

    // Submit Button
    var submitButton = document.createElement("button");
    submitButton.id = valuateId + "_submit";
    submitButton.setAttribute("onClick", "valuateSend('" + valuateId + "')");
    submitButton.appendChild(document.createTextNode("Send Evaluation"));
    submitButton.setAttribute("class", "valuate-submit");
    submitButton.disabled = true;

    valuate.appendChild(submitButton);
}

function valuateChangeIdentify(id) {
    var valuate = document.getElementById(id);
    var identifyLabel = document.getElementById(id + "_identify_label");
    if (document.getElementById(id + "_identify").checked) {
        identifyLabel.innerHTML = String.format(identificationFormat, valuate.getAttribute("valuator-id"));
    } else {
        identifyLabel.innerHTML = anonymousFormat;
    }
}

function valuateClose(id) { // Removing a valuate element from the page
    var valuate = document.getElementById(id);
    valuate.parentElement.removeChild(valuate);
}

function valuateResetGrade(id) {   // Remove the existing grade from a valuate element, for the sake of setting the new one.
    var valuate = document.getElementById(id);
    var grade = valuate.getAttribute("grade");

    if (grade > 0) {
        var selected = document.getElementById(valuate.getAttribute("id") + "_grade_" + grade);
        selected.removeAttribute("selected");
    }
    valuate.removeAttribute("grade");
}

function valuateGrade(id, grade) {  // Setting a grade for a valuate element with a given id.
    valuateResetGrade(id);

    var valuate = document.getElementById(id);
    valuate.setAttribute("grade", grade);
    document.getElementById(id + "_grade_" + grade).setAttribute("selected", "");
    valuateEnableSend(id);
}

function valuateEnableSend(id) {    // Enabling the submitt button for a valuate element with a given id, once the grade is selected.
    var valuate = document.getElementById(id);

    var submitButton = document.getElementById(id + "_submit");
    submitButton.disabled = (valuate.getAttribute("grade") < 1);    // Only the grade is mandatory for the time being.
}

function valuateSend(id) {
    var valuate = document.getElementById(id);
    var valuateId = valuate.getAttribute("id");

    var xhttp = new XMLHttpRequest();
    xhttp.open("POST", valuateServiceUrl + "/endpoint", false);

    var data = {};

    data.siteId = valuateSiteId; // Appended as a constant by the servlet.
    data.questionId = valuateId.split(servicePrefix)[1];   // Trimming off the local service prefix

    // Setting attributes - everything is String
    data.grade = valuate.getAttribute("grade");
    data.question = document.getElementById(valuateId + "_question").innerHTML.trim();
    data.comment = document.getElementById(valuateId + "_comment").value;
    data.lowest = document.getElementById(valuateId + "_lowest").innerHTML;
    data.highest = document.getElementById(valuateId + "_highest").innerHTML;
    data.fullUrl = window.location.href;

    var valuatorId = valuate.getAttribute("valuator-id");
    if (valuatorId !== null && document.getElementById(valuateId + "_identify").checked) {
        data.valuatorId = valuatorId;
    }

    var reference = valuate.getAttribute("reference");
    if (reference !== null) {
        data.reference = reference;
    }

    xhttp.send(JSON.stringify(data));

    valuate.innerHTML = ""; // Deleting everything from the valuate element after sending a feedback.
    valuate.setAttribute("class", "valuate-sent");  // Setting a css class for an after-submit message.

    if (xhttp.status === 200) {
        valuate.innerHTML = submitSuccessMessage;
    } else {
        valuate.innerHTML = submitFailMessage;
    }
}