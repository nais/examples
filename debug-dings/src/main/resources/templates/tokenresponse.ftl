<#import "main.ftl" as layout />

<@layout.mainLayout title="TokenDings Client Debugger" description="Just a debugging client">
    <div class="container">
        <section class="header">
            <h2 class="title">TokenDings Client Debugger</h2>
        </section>

        <div class="docs-section" id="openid">
            <h3 class="docs-header">OAuth 2.0 Token Exchange</h3>
            <p>Inspect the actual token response</p>
            <div>
                <label for="token_request">Token Request</label>
                <pre id="token_request"><code>${token_request}</code></pre>
                <label>Token Response</label>
                <pre><code>${token_response}</code></pre>
            </div>
        </div>
        <div class="docs-section" id="forms">
            <h6 class="docs-header">Call downstream API with Bearer token</h6>
            <p>More options will be added shortly</p>
            <form method="post" action="/debugger/call">
                <div class="row">
                    <div class="twelve columns">
                        <label for="api_url">Url</label>
                        <input class="u-full-width" type="text" placeholder="The API url"
                               name="api_url" value="${api_url}">
                        <label for="bearer_token">Bearer token</label>
                        <input class="u-full-width" type="text" placeholder="Bearer token"
                               name="bearer_token" value="${bearer_token}">
                        <input class="button-primary" type="submit" value="Call API">
                    </div>
                </div>
            </form>
        </div>
    </div>
</@layout.mainLayout>
