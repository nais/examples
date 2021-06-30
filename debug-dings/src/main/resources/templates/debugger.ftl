<#import "main.ftl" as layout />

<@layout.mainLayout title="TokenDings Client Debugger" description="Just a debugger client using tokendings">
    <div class="container">
        <section class="header">
            <h2 class="title">TokenDings Client Debugger</h2>
        </section>

        <div class="docs-section" id="openid">

            <h6 class="docs-header">OAuth 2.0 Token Exchange</h6>
            <form method="post">
                <div class="row">
                    <div class="twelve columns">
                        <label for="tokendings_url">Url</label>
                        <input class="u-full-width" type="text" placeholder="The Token endpoint"
                               name="tokendings_url" value="${tokendings_url}">
                    </div>
                </div>
                <h6 class="docs-header">Form Parameters</h6>
                <p>Insert your parameters here to get a token from your OAuth 2.0 Authorization Server</p>
                <div class="row">
                    <div class="twelve columns">
                        <label for="client_assertion_type">client_assertion_type</label>
                        <input class="u-full-width" type="text" placeholder="" name="client_assertion_type"
                               value="${client_assertion_type}">

                        <label for="client_assertion">client_assertion</label>

                        <input class="u-full-width" type="text" placeholder=""
                               name="client_assertion" value="${client_assertion}">

                        <label for="grant_type">grant_type</label>
                        <input class="u-full-width" type="text"
                               placeholder="" name="grant_type"
                               value="${grant_type}">
                        <label for="subject_token_type">subject_token_type</label>
                        <input class="u-full-width" type="text" placeholder=""
                               name="subject_token_type" value="${subject_token_type}">
                        <label for="subject_token">subject_token</label>
                        <input class="u-full-width" type="text" placeholder=""
                               name="subject_token" value="${subject_token}">
                        <label for="audience">audience</label>
                        <input class="u-full-width" type="text" placeholder=""
                               name="audience" value="${audience}">
                        <input class="button-primary" type="submit" value="Get a token">
                    </div>
            </form>
        </div>
    </div>
</@layout.mainLayout>
