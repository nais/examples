<#import "main.ftl" as layout />

<@layout.mainLayout title="TokenDings Debugger" description="Just a debugging client">
    <div class="container">
        <section class="header">
            <h2 class="title">TokenDings Client Debugger</h2>
        </section>

        <div class="docs-section" id="openid">
            <h3 class="docs-header">Call downstream API</h3>
            <div>
                <label for="api_request">API Request</label>
                <pre id="api_request"><code>${api_request}</code></pre>
                <label>API Response</label>
                <pre><code>${api_response}</code></pre>
            </div>
        </div>
    </div>
</@layout.mainLayout>
