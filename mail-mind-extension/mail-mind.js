console.log("Mail Mind Extension Loaded");

// Configuration and state management
const AssistantConfig = {
    API_ENDPOINT: 'http://localhost:8080/api/mail/compose-reply',
    TONE_OPTIONS: [
        { value: 'professional', label: 'Professional' },
        { value: 'friendly', label: 'Friendly' },
        { value: 'formal', label: 'Formal' },
        { value: 'casual', label: 'Casual' },
        { value: 'apologetic', label: 'Apologetic' },
        { value: 'enthusiastic', label: 'Enthusiastic' }
    ],
    BUTTON_STATES: {
        IDLE: 'Smart Reply',
        PROCESSING: 'Crafting...',
    }
};

let currentTonePreference = 'professional';

// Reply button
function constructSmartReplyButton() {
    const smartButton = document.createElement('div');
    smartButton.classList.add('smart-reply-assistant');
    smartButton.innerHTML = AssistantConfig.BUTTON_STATES.IDLE;
    smartButton.setAttribute('role', 'button');
    smartButton.setAttribute('data-tooltip', 'Generate Smart Reply');
    smartButton.setAttribute('tabindex', '0');

    return smartButton;
}

// Tone selector UI component
function createToneSelector() {
    const toneContainer = document.createElement('div');
    toneContainer.className = 'smart-tone-selector';

    const headerElement = document.createElement('h3');
    headerElement.textContent = 'Select Response Tone';

    const optionsGrid = document.createElement('div');
    optionsGrid.classList.add('options-grid');

    AssistantConfig.TONE_OPTIONS.forEach(toneOption => {
        const optionButton = document.createElement('button');
        optionButton.textContent = toneOption.label;
        if (currentTonePreference === toneOption.value) {
            optionButton.classList.add('selected-tone');
        }

        optionButton.addEventListener('click', () => {
            currentTonePreference = toneOption.value;
            updateToneSelection(optionsGrid);
        });

        optionsGrid.appendChild(optionButton);
    });

    const actionButtons = document.createElement('div');
    actionButtons.classList.add('action-buttons');

    const applyButton = document.createElement('button');
    applyButton.textContent = 'Apply & Generate';

    applyButton.addEventListener('click', () => {
        toneContainer.style.display = 'none';
        initiateSmartReply();
    });

    actionButtons.appendChild(applyButton);

    toneContainer.appendChild(headerElement);
    toneContainer.appendChild(optionsGrid);
    toneContainer.appendChild(actionButtons);

    return toneContainer;
}

function updateToneSelection(gridContainer) {
    const buttons = gridContainer.querySelectorAll('button');
    buttons.forEach((btn, index) => {
        const isSelected = AssistantConfig.TONE_OPTIONS[index].value === currentTonePreference;
        if (isSelected) {
            btn.classList.add('selected-tone');
        } else {
            btn.classList.remove('selected-tone');
        }
    });
}

// Email content extraction
function extractEmailContext() {
    const contentSelectors = [
        '.h7',
        '.a3s.aiL',
        '.gmail_quote',
        '[role="presentation"]'
    ];

    for (const selector of contentSelectors) {
        const contentElement = document.querySelector(selector);
        if (contentElement && contentElement.innerText.trim()) {
            return contentElement.innerText.trim();
        }
    }
    return '';
}

// Toolbar detection
function locateComposeToolbar() {
    const toolbarSelectors = [
        '.btC',
        '.aDh',
        '.gU.Up'
    ];

    for (const selector of toolbarSelectors) {
        const toolbarElement = document.querySelector(selector);
        if (toolbarElement) {
            return toolbarElement;
        }
    }
    return null;
}

// Main smart reply generation function
async function initiateSmartReply() {
    const smartButton = document.querySelector('.smart-reply-assistant');
    if (!smartButton) return;

    try {
        smartButton.innerHTML = AssistantConfig.BUTTON_STATES.PROCESSING;
        smartButton.disabled = true;

        const emailContext = extractEmailContext();
        if (!emailContext) {
            throw new Error('No email content found to reply to');
        }

        const apiResponse = await fetch(AssistantConfig.API_ENDPOINT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                emailContent: emailContext,
                tone: currentTonePreference
            })
        });

        if (!apiResponse.ok) {
            throw new Error(`API request failed: ${apiResponse.status}`);
        }

        const generatedResponse = await apiResponse.text();

        // Insert into compose box
        const composeTextArea = document.querySelector('[role="textbox"][g_editable="true"]');
        if (composeTextArea) {
            composeTextArea.focus();

            // Clear existing content and insert new
            composeTextArea.innerHTML = '';
            document.execCommand('insertText', false, generatedResponse);

            // Trigger input event for Gmail
            const inputEvent = new Event('input', { bubbles: true });
            composeTextArea.dispatchEvent(inputEvent);
        } else {
            throw new Error('Compose area not accessible');
        }

    } catch (processingError) {
        console.error('Smart reply generation failed:', processingError);
        alert(`Failed to generate smart reply: ${processingError.message}`);
    } finally {
        // Reset button state
        setTimeout(() => {
            smartButton.innerHTML = AssistantConfig.BUTTON_STATES.IDLE;
            smartButton.disabled = false;
        }, 1000);
    }
}

// Button injection with tone selector
function injectSmartReplyInterface() {
    const existingInterface = document.querySelector('.smart-reply-assistant');
    if (existingInterface) existingInterface.remove();

    const targetToolbar = locateComposeToolbar();
    if (!targetToolbar) {
        console.log("Compose toolbar not detected");
        return;
    }

    console.log("Compose toolbar detected, injecting smart reply interface");

    const buttonContainer = document.createElement('div');
    buttonContainer.style.cssText = `
        position: relative;
        display: inline-block;
        margin-right: 8px;
    `;

    const smartButton = constructSmartReplyButton();
    const toneSelector = createToneSelector();

    // Button click handler with tone selection
    smartButton.addEventListener('click', (event) => {
        event.stopPropagation();
        const isVisible = toneSelector.style.display === 'block';
        toneSelector.style.display = isVisible ? 'none' : 'block';
    });

    // Close tone selector when clicking outside
    document.addEventListener('click', (event) => {
        if (!buttonContainer.contains(event.target)) {
            toneSelector.style.display = 'none';
        }
    });

    buttonContainer.appendChild(smartButton);
    buttonContainer.appendChild(toneSelector);
    targetToolbar.insertBefore(buttonContainer, targetToolbar.firstChild);

    const optionsGrid = toneSelector.querySelector('.options-grid');
    if (optionsGrid) {
        updateToneSelection(optionsGrid);
    }
}

// Mutation observer for dynamic content
const interfaceObserver = new MutationObserver((mutationsList) => {
    for (const mutation of mutationsList) {
        const addedElements = Array.from(mutation.addedNodes);
        const hasComposeInterface = addedElements.some(node =>
            node.nodeType === Node.ELEMENT_NODE &&
            (node.matches('.aDh, .btC, [role="dialog"]') ||
             node.querySelector('.aDh, .btC, [role="dialog"]'))
        );

        if (hasComposeInterface) {
            console.log("New compose interface detected");
            setTimeout(injectSmartReplyInterface, 600);
        }
    }
});

// Initialize the extension
interfaceObserver.observe(document.body, {
    childList: true,
    subtree: true
});

// Initial injection after a delay to ensure Gmail is fully loaded
setTimeout(injectSmartReplyInterface, 1500);